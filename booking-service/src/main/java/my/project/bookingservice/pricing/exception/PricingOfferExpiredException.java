package my.project.bookingservice.pricing.exception;

import my.project.common.exception.ConflictException;

public class PricingOfferExpiredException extends ConflictException {
	public PricingOfferExpiredException(String message) {
		super(message);
	}

	public PricingOfferExpiredException(String message, Throwable cause) {
		super(message, cause);
	}
}

