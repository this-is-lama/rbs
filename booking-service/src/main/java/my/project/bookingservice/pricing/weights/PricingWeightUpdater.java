package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PricingWeightUpdater {
	private final PricingProperties properties;

	public BigDecimal smoothUpdate(BigDecimal currentValue, BigDecimal recommendedValue) {
		BigDecimal eta = properties.getHistory().getWeightUpdateRate();
		BigDecimal updated = currentValue.add(eta.multiply(recommendedValue.subtract(currentValue)));
		return updated.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : updated;
	}
}

