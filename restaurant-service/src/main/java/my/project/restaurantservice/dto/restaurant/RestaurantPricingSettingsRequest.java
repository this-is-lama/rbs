package my.project.restaurantservice.dto.restaurant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RestaurantPricingSettingsRequest(
		@NotNull
		@DecimalMin("0.00")
		BigDecimal minPricingCharge,

		@NotNull
		@DecimalMin("0.00")
		BigDecimal maxPricingCharge
) {
}

