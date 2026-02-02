package my.project.userservice.exception;

import my.project.common.exception.ApiException;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.ErrorCode;

public class InvalidCredentialsException extends ApiException {

	public InvalidCredentialsException(ErrorCode code, String message, Object... args) {
		super(code, message, args);
	}

	public InvalidCredentialsException(String message, Object... args) {
		super(CommonErrorCode.INVALID_CREDENTIALS, message, args);
	}
}
