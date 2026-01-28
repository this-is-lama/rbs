package my.project.common.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }

    public ForbiddenException(String message, Object... args) {
        super(CommonErrorCode.FORBIDDEN, message, args);
    }
}