package my.project.bookingservice.pricing.weights;

import my.project.bookingservice.pricing.enums.PricingWeightCode;

import java.math.BigDecimal;
import java.util.UUID;

public interface PricingWeightProvider {
	BigDecimal getWeight(UUID restaurantId, PricingWeightCode code);
}

