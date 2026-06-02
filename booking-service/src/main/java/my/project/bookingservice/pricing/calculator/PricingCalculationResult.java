package my.project.bookingservice.pricing.calculator;

import java.math.BigDecimal;
import java.time.Instant;

public record PricingCalculationResult(
		BigDecimal preorderAmount,
		BigDecimal pricingCharge,
		BigDecimal totalAmount,
		BigDecimal demandIndex,
		BigDecimal loadBlockValue,
		BigDecimal historicalDemandBlockValue,
		BigDecimal calendarContextBlockValue,
		Instant calculatedAt
) {
}

