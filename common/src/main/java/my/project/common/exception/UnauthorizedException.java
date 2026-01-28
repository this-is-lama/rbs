package my.project.common.exception;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }

    public UnauthorizedException(String message, Object... args) {
        super(CommonErrorCode.UNAUTHORIZED, message, args);
    }
}