package my.project.bookingservice.pricing.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PricingPreorderItemRequest(
		@NotNull UUID dishId,
		@NotNull @Min(1) @Max(100) Integer quantity
) {
}

