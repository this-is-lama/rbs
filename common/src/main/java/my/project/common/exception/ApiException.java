package my.project.common.exception;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final Object[] args;

    protected ApiException(ErrorCode code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }

}
