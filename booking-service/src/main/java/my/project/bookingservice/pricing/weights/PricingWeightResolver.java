package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingWeightResolver {
	private final PricingWeightProvider weightProvider;
	private final PricingProperties properties;

	public Map<PricingWeightCode, BigDecimal> resolveNormalized(UUID restaurantId, List<PricingWeightCode> codes) {
		Map<PricingWeightCode, BigDecimal> source = new EnumMap<>(PricingWeightCode.class);
		for (PricingWeightCode code : codes) {
			source.put(code, weightProvider.getWeight(restaurantId, code));
		}

		if (positiveSum(source).compareTo(BigDecimal.ZERO) <= 0) {
			source.clear();
			for (PricingWeightCode code : codes) {
				source.put(code, defaultWeight(code));
			}
		}

		Map<PricingWeightCode, BigDecimal> normalized = NormalizationUtils.normalizeWeights(source);
		Map<PricingWeightCode, BigDecimal> result = new EnumMap<>(PricingWeightCode.class);
		for (PricingWeightCode code : codes) {
			result.put(code, normalized.getOrDefault(code, BigDecimal.ZERO));
		}
		return result;
	}

	public BigDecimal defaultWeight(PricingWeightCode code) {
		return properties.getDefaults().getWeights().getOrDefault(code.name(), BigDecimal.ZERO);
	}

	private BigDecimal positiveSum(Map<PricingWeightCode, BigDecimal> source) {
		return source.values().stream()
				.filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
