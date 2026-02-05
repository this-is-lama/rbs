package my.project.bookingservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import my.project.common.dto.ApiError;
import my.project.common.exception.ApiException;

import java.io.InputStream;

@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

	private final ObjectMapper objectMapper;
	private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

	@Override
	public Exception decode(String methodName, Response response) {
		Response.Body body = response.body();
		if (body == null) {
			return defaultDecoder.decode(methodName, response);
		}
		try (InputStream input = body.asInputStream()) {
			ApiError error = objectMapper.readValue(input, ApiError.class);
			return new ApiException(error);
		} catch (Exception e) {
			return defaultDecoder.decode(methodName, response);
		}
	}
}


