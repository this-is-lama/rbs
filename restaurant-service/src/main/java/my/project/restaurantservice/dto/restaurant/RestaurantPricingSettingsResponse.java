package my.project.restaurantservice.dto.restaurant;

import java.math.BigDecimal;

public record RestaurantPricingSettingsResponse(
		BigDecimal minPricingCharge,
		BigDecimal maxPricingCharge
) {
}

