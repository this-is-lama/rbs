package my.project.userservice.exception;

import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final ExceptionCode code;

    protected ApiException(ExceptionCode code, String message) {
        super(message);
        this.code = code;
    }

}
