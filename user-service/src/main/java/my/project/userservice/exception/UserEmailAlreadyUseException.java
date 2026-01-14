package my.project.userservice.exception;

public class UserEmailAlreadyUseException extends ApiException {

	public UserEmailAlreadyUseException() {
		super(ExceptionCode.USER_EMAIL_ALREADY_USE, "Пользователь с такой почтой уже существует");
	}
}
