package my.project.userservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND),
	USER_NOT_ENABLED(HttpStatus.FORBIDDEN),
	USER_EMAIL_ALREADY_USE(HttpStatus.CONFLICT),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),;

	private final HttpStatus status;
}
