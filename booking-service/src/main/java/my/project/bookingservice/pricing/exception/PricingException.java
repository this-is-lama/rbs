package my.project.bookingservice.pricing.exception;

import my.project.common.exception.ValidationException;

public class PricingException extends ValidationException {
	public PricingException(String message) {
		super(message);
	}

	public PricingException(String message, Throwable cause) {
		super(message, cause);
	}
}

