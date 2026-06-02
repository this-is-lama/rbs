package my.project.common.exception;

public class ConflictException extends ApiException {

    public ConflictException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }

    public ConflictException(ErrorCode code, String message, Throwable cause, Object... args) {
        super(code, message, cause, args);
    }

    public ConflictException(String message, Object... args) {
        super(CommonErrorCode.CONFLICT, message, args);
    }

    public ConflictException(String message, Throwable cause, Object... args) {
        super(CommonErrorCode.CONFLICT, message, cause, args);
    }
}
