package my.project.bookingservice.pricing.blocks;

import my.project.bookingservice.pricing.enums.PricingBlockCode;

import java.math.BigDecimal;
import java.util.Map;

public record BlockCalculationResult(
		PricingBlockCode code,
		BigDecimal value,
		Map<String, BigDecimal> details
) {
}

