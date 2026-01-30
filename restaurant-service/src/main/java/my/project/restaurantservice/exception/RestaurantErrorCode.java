package my.project.restaurantservice.exception;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RestaurantErrorCode implements ErrorCode {

	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),;

	private final HttpStatus status;

	@Override
	public HttpStatus status() {
		return status;
	}

	@Override
	public String code() {
		return name();
	}
}
