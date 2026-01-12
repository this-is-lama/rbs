package my.project.userservice.exception;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(String email) {
        super(ExceptionCode.USER_NOT_FOUND, "Пользователь с email '%s' не найден".formatted(email));
    }
}
