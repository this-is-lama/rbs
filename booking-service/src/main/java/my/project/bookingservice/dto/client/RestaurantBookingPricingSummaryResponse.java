package my.project.bookingservice.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record RestaurantBookingPricingSummaryResponse(
        UUID restaurantId,
        Integer totalTablesCount,
        BigDecimal minPricingCharge,
        BigDecimal maxPricingCharge
) {
}
