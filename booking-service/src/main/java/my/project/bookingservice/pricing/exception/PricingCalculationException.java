package my.project.bookingservice.pricing.exception;

public class PricingCalculationException extends PricingException {
	public PricingCalculationException(String message) {
		super(message);
	}

	public PricingCalculationException(String message, Throwable cause) {
		super(message, cause);
	}
}

