package my.project.userservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.userservice.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
		HttpStatus status = ex.getCode().getStatus();

		if (status.is5xxServerError()) {
			log.error("ApiException: code={}, path={}, message={}", ex.getCode(), request.getRequestURI(), ex.getMessage(), ex);
		} else {
			log.warn("ApiException: code={}, path={}, message={}", ex.getCode(), request.getRequestURI(), ex.getMessage());
		}

		ApiError body = new ApiError(
				status.value(),
				ex.getCode().name(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception: path={}", request.getRequestURI(), ex);

		ApiError body = new ApiError(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"INTERNAL_ERROR",
				"Внутренняя ошибка сервера",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

}
