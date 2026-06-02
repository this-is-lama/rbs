package my.project.bookingservice.pricing.history.model;

import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PricingHistoryObservation(
		UUID restaurantId,
		UUID tableId,
		PricingHistoryObservationType observationType,
		LocalDate observationDate,
		String timeIntervalCode,
		Integer successfulBookingsCount,
		Integer availableTablesCount,
		BigDecimal occupancyValue,
		BigDecimal urgencyValue,
		BigDecimal weekdayDemandValue,
		BigDecimal timeIntervalDemandValue,
		BigDecimal tableDemandValue,
		BigDecimal calendarStatusValue,
		BigDecimal realizedDemandValue
) {
}

