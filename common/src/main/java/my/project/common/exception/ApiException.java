package my.project.common.exception;

import lombok.Getter;
import my.project.common.dto.ApiError;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final Object[] args;

    public ApiException(ErrorCode code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

    public ApiException(ErrorCode code, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }

    public ApiException(ApiError error) {
        super(error.message());
        ErrorCode errorCode;
        try {
            errorCode = CommonErrorCode.valueOf(error.code());
        } catch (IllegalArgumentException e) {
            errorCode = CommonErrorCode.INTERNAL_ERROR;
        }
        this.code = errorCode;
        this.args = null;
    }

}
