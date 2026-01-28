package my.project.restaurantservice.exception;

import my.project.common.exception.ApiException;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.ErrorCode;

public class StorageException extends ApiException {

    public StorageException(ErrorCode code, String message, Object... args) {
        super(code, message, args);
    }

    public StorageException(String message, Object... args) {
        super(CommonErrorCode.SERVICE_UNAVAILABLE, message, args);
    }
}
