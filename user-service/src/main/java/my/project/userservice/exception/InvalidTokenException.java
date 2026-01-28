package my.project.userservice.exception;

import my.project.common.exception.ApiException;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.ErrorCode;

public class InvalidTokenException extends ApiException {

	public InvalidTokenException(ErrorCode code, String message, Object... args) {
		super(code, message, args);
	}

	public InvalidTokenException(String message, Object... args) {
		super(CommonErrorCode.UNAUTHORIZED, message, args);
	}
}
