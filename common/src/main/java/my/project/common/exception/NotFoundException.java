package my.project.common.exception;

public class NotFoundException extends ApiException {

    public NotFoundException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }

    public NotFoundException(String message, Object... args) {
        super(CommonErrorCode.NOT_FOUND, message, args);
    }
}