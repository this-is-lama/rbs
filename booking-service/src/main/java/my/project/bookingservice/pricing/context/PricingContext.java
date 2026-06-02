package my.project.bookingservice.pricing.context;

import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PricingContext(
		UUID userId,
		UUID restaurantId,
		UUID tableId,
		Instant visitStart,
		Instant visitEnd,
		Instant requestTime,
		List<PricingPreorderItemRequest> preorderItems,
		BigDecimal preorderAmount,
		String cartHash,
		BigDecimal restaurantMinPricingCharge,
		BigDecimal restaurantMaxPricingCharge,
		Integer totalTablesCount,
		Integer occupiedTablesCount
) {
	public boolean hasPreorder() {
		return preorderItems != null && !preorderItems.isEmpty();
	}
}

