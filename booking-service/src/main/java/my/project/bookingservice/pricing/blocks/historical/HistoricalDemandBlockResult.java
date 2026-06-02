package my.project.bookingservice.pricing.blocks.historical;

import java.math.BigDecimal;

public record HistoricalDemandBlockResult(
		BigDecimal value,
		BigDecimal weekdayDemand,
		BigDecimal timeIntervalDemand,
		BigDecimal tableDemand
) {
}

