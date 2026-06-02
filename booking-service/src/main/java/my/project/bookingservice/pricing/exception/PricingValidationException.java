package my.project.bookingservice.pricing.exception;

public class PricingValidationException extends PricingException {
	public PricingValidationException(String message) {
		super(message);
	}

	public PricingValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}

