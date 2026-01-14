package my.project.userservice.exception;

public class InvalidTokenException extends ApiException {

	public InvalidTokenException() {
		super(ExceptionCode.INVALID_TOKEN, "Некорректный токен");
	}
}
