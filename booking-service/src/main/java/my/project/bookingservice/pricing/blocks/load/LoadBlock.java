package my.project.bookingservice.pricing.blocks.load;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.blocks.BlockCalculationResult;
import my.project.bookingservice.pricing.blocks.PricingBlock;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingBlockCode;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.parameters.load.OccupancyParameter;
import my.project.bookingservice.pricing.parameters.load.UrgencyParameter;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.weights.PricingWeightProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoadBlock implements PricingBlock {
	private final OccupancyParameter occupancyParameter;
	private final UrgencyParameter urgencyParameter;
	private final PricingWeightProvider weightProvider;
	private final PricingProperties properties;

	@Override
	public BlockCalculationResult calculate(PricingContext context) {
		BigDecimal occupancy = occupancyParameter.calculate(context).value();
		BigDecimal urgency = urgencyParameter.calculate(context).value();
		BigDecimal interaction = occupancy.multiply(urgency);
		Map<PricingWeightCode, BigDecimal> weights = normalizedWeights(context);
		BigDecimal value = weights.get(PricingWeightCode.OCCUPANCY_PARAMETER).multiply(occupancy)
				.add(weights.get(PricingWeightCode.URGENCY_PARAMETER).multiply(urgency))
				.add(weights.get(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION).multiply(interaction));
		Map<String, BigDecimal> details = new LinkedHashMap<>();
		details.put("occupancy", occupancy);
		details.put("urgency", urgency);
		details.put("interaction", interaction);
		return new BlockCalculationResult(PricingBlockCode.LOAD, NormalizationUtils.clamp01(value), details);
	}

	private Map<PricingWeightCode, BigDecimal> normalizedWeights(PricingContext context) {
		Map<PricingWeightCode, BigDecimal> source = new EnumMap<>(PricingWeightCode.class);
		source.put(PricingWeightCode.OCCUPANCY_PARAMETER, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.OCCUPANCY_PARAMETER));
		source.put(PricingWeightCode.URGENCY_PARAMETER, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.URGENCY_PARAMETER));
		source.put(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION));
		if (positiveSum(source).compareTo(BigDecimal.ZERO) <= 0) {
			source = new EnumMap<>(PricingWeightCode.class);
			source.put(PricingWeightCode.OCCUPANCY_PARAMETER, defaultWeight(PricingWeightCode.OCCUPANCY_PARAMETER));
			source.put(PricingWeightCode.URGENCY_PARAMETER, defaultWeight(PricingWeightCode.URGENCY_PARAMETER));
			source.put(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION, defaultWeight(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION));
		}
		return NormalizationUtils.normalizeWeights(source);
	}

	private BigDecimal positiveSum(Map<PricingWeightCode, BigDecimal> source) {
		return source.values().stream()
				.filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal defaultWeight(PricingWeightCode code) {
		return properties.getDefaults().getWeights().getOrDefault(code.name(), BigDecimal.ZERO);
	}
}

