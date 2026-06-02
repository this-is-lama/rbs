package my.project.bookingservice.pricing.weights.result;

import my.project.bookingservice.pricing.enums.PricingWeightCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WeightCalculationResult(
		UUID restaurantId,
		List<Item> items
) {
	public record Item(PricingWeightCode code, BigDecimal recommendedValue) {
	}
}

