package my.project.userservice.exception;

import java.util.UUID;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(String email) {
        super(ExceptionCode.USER_NOT_FOUND, "Пользователь с email '%s' не найден".formatted(email));
    }

    public UserNotFoundException(UUID id) {
        super(ExceptionCode.USER_NOT_FOUND, "Пользователь с id '%s' не найден".formatted(id));
    }
}
