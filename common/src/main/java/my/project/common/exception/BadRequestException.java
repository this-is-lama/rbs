package my.project.common.exception;

public class BadRequestException extends ApiException {

    public BadRequestException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }

    public BadRequestException(String message, Object... args) {
        super(CommonErrorCode.BAD_REQUEST, message, args);
    }
}


















