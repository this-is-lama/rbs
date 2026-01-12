package my.project.userservice.exception;

public class InvalidCredentialsException extends ApiException {

	public InvalidCredentialsException() {
		super(ExceptionCode.INVALID_CREDENTIALS, "Неправильный логин или пароль");
	}
}
