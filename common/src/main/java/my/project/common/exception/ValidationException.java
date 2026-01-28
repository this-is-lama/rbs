package my.project.common.exception;


public class ValidationException extends ApiException {

    public ValidationException(String message, Object... args) {
        super(CommonErrorCode.VALIDATION_ERROR, message, args);
    }

    public ValidationException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }
}