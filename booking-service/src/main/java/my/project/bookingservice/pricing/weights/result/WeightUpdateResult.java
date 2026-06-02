package my.project.bookingservice.pricing.weights.result;

import my.project.bookingservice.pricing.enums.PricingWeightCode;

import java.math.BigDecimal;
import java.util.UUID;

public record WeightUpdateResult(
		UUID restaurantId,
		PricingWeightCode code,
		BigDecimal previousValue,
		BigDecimal newValue
) {
}

