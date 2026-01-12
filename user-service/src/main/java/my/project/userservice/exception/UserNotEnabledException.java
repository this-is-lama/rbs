package my.project.userservice.exception;

public class UserNotEnabledException extends ApiException {

	public UserNotEnabledException(String email) {
		super(ExceptionCode.USER_NOT_ENABLED, "Пользователь с email '%s' отключен".formatted(email));
	}
}
