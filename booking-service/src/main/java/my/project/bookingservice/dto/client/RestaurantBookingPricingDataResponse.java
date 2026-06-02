package my.project.bookingservice.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record RestaurantBookingPricingDataResponse(
        UUID restaurantId,
        UUID tableId,
        Integer totalTablesCount,
        BigDecimal minPricingCharge,
        BigDecimal maxPricingCharge
) {
}
