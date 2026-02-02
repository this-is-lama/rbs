package my.project.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

	BAD_REQUEST(HttpStatus.BAD_REQUEST),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST),

	UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
	FORBIDDEN(HttpStatus.FORBIDDEN),

	NOT_FOUND(HttpStatus.NOT_FOUND),
	CONFLICT(HttpStatus.CONFLICT),

	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED),

	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),;

	private final HttpStatus status;

	@Override
	public HttpStatus status() {
		return status;
	}

	@Override
	public String code() {
		return name();
	}
}
