package my.project.restaurantservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.dto.ApiError;
import my.project.common.exception.ApiException;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

	private final ObjectMapper objectMapper;
	private final ErrorDecoder defaultDecoder = new Default();

	@Override
	public Exception decode(String methodName, Response response) {
		Response.Body body = response.body();
		if (body == null) {
			log.warn("Feign-ответ без тела, используется стандартный декодер ошибок, method={}", methodName);
			return defaultDecoder.decode(methodName, response);
		}

		try (InputStream input = body.asInputStream()) {
			ApiError error = objectMapper.readValue(input, ApiError.class);
			log.warn("Получена ошибка от внешнего сервиса через Feign, method={}, status={}, code={}",
					methodName, response.status(), error.code());
			return new ApiException(error);
		} catch (Exception e) {
			log.warn("Не удалось декодировать ошибку Feign-ответа, method={}, status={}",
					methodName, response.status(), e);
			return defaultDecoder.decode(methodName, response);
		}
	}
}