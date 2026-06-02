package my.project.bookingservice.pricing.dto.response;

import my.project.bookingservice.pricing.enums.PricingOfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PricingOfferResponse(
		UUID offerId,
		UUID restaurantId,
		UUID tableId,
		BigDecimal preorderAmount,
		BigDecimal pricingCharge,
		BigDecimal totalAmount,
		String currency,
		PricingOfferStatus status,
		Instant calculatedAt,
		Instant expiresAt
) {
}

