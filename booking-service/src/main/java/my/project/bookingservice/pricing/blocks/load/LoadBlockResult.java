package my.project.bookingservice.pricing.blocks.load;

import java.math.BigDecimal;

public record LoadBlockResult(
		BigDecimal value,
		BigDecimal occupancy,
		BigDecimal urgency,
		BigDecimal interaction
) {
}

