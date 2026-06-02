package my.project.bookingservice.pricing.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PricingPreorderItemResponse(
		UUID dishId,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal amount
) {
}

