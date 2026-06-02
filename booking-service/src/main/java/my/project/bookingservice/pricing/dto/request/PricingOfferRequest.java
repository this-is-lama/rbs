package my.project.bookingservice.pricing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PricingOfferRequest(
		@NotNull UUID restaurantId,
		@NotNull UUID tableId,
		@NotNull Instant startAt,
		@NotNull Instant endAt,
		@Valid
		@Size(max = 50)
		List<PricingPreorderItemRequest> preorderItems
) {
}

