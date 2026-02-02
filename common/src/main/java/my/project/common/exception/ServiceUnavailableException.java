package my.project.common.exception;

public class ServiceUnavailableException extends ApiException{

	public ServiceUnavailableException(ErrorCode code, String message, Object... args) {
		super(code, message, args);
	}

	public ServiceUnavailableException(String message, Object... args) {
		super(CommonErrorCode.SERVICE_UNAVAILABLE, message, args);
	}
}
