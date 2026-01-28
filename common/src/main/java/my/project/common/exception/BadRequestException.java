package my.project.common.exception;

public class BadRequestException extends ApiException {

    protected BadRequestException(ErrorCode code, String message, Object... args) {
        super(CommonErrorCode.BAD_REQUEST, message, args);
    }
}


















