package my.project.restaurantservice.exception;

import my.project.common.dto.ApiError;
import my.project.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {

		HttpStatus status = ex.getCode().status();
		var locale = LocaleContextHolder.getLocale();

		if (status.is5xxServerError()) {
			log.error("ApiException: code={}, path={}, message={}", ex.getCode(), request.getRequestURI(), ex.getMessage(), ex);
		} else {
			log.warn("ApiException: code={}, path={}, message={}", ex.getCode(), request.getRequestURI(), ex.getMessage());
		}

		ApiError body = new ApiError(
				status.value(),
				ex.getCode().code(),
				messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale),
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception: path={}", request.getRequestURI(), ex);

		ApiError body = new ApiError(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.name(),
				"common.internal-error",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

}