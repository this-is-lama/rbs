package my.project.userservice.exception;

import java.util.UUID;

public class UserNotEnabledException extends ApiException {

	public UserNotEnabledException(UUID id) {
		super(ExceptionCode.USER_NOT_ENABLED, "Пользователь с id '%s' отключен".formatted(id));
	}
}
