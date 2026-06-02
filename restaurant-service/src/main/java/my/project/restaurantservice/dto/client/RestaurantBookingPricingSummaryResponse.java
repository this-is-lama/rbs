package my.project.restaurantservice.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record RestaurantBookingPricingSummaryResponse(
        UUID restaurantId,
        Integer totalTablesCount,
        BigDecimal minPricingCharge,
        BigDecimal maxPricingCharge
) {
}
