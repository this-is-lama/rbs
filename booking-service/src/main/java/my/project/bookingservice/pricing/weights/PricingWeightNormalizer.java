package my.project.bookingservice.pricing.weights;

import my.project.bookingservice.pricing.util.NormalizationUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PricingWeightNormalizer {
	public <K> Map<K, BigDecimal> normalize(Map<K, BigDecimal> weights) {
		return NormalizationUtils.normalizeWeights(weights);
	}
}

