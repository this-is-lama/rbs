package my.project.bookingservice.pricing.parameters;

import my.project.bookingservice.pricing.enums.PricingParameterCode;
import my.project.bookingservice.pricing.enums.PricingValueSource;

import java.math.BigDecimal;

public record ParameterCalculationResult(
		PricingParameterCode code,
		BigDecimal value,
		PricingValueSource source
) {
}

