package my.project.bookingservice.pricing.exception;

public class PricingSettingsNotFoundException extends PricingException {
	public PricingSettingsNotFoundException(String message) {
		super(message);
	}

	public PricingSettingsNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}

