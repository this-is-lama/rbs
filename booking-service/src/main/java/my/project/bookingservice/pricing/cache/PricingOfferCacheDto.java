package my.project.bookingservice.pricing.cache;

import my.project.bookingservice.pricing.enums.PricingOfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PricingOfferCacheDto(
		UUID offerId,
		UUID userId,
		UUID restaurantId,
		UUID tableId,
		Instant visitStart,
		Instant visitEnd,
		String cartHash,
		BigDecimal preorderAmount,
		BigDecimal pricingCharge,
		BigDecimal totalAmount,
		BigDecimal demandIndex,
		BigDecimal loadBlockValue,
		BigDecimal historicalDemandBlockValue,
		BigDecimal calendarContextBlockValue,
		String currency,
		PricingOfferStatus status,
		Instant calculatedAt,
		Instant expiresAt
) {
}
