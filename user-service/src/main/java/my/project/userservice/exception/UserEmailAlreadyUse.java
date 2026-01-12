package my.project.userservice.exception;

public class UserEmailAlreadyUse extends ApiException {

	public UserEmailAlreadyUse() {
		super(ExceptionCode.USER_EMAIL_ALREADY_USE, "Пользователь с такой почтой уже существует");
	}
}
