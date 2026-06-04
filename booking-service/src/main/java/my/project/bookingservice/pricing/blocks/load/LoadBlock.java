package my.project.bookingservice.pricing.blocks.load;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.blocks.BlockCalculationResult;
import my.project.bookingservice.pricing.blocks.PricingBlock;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingBlockCode;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.parameters.load.OccupancyParameter;
import my.project.bookingservice.pricing.parameters.load.UrgencyParameter;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.weights.PricingWeightResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoadBlock implements PricingBlock {
	private static final List<PricingWeightCode> WEIGHTS = List.of(
			PricingWeightCode.OCCUPANCY_PARAMETER,
			PricingWeightCode.URGENCY_PARAMETER,
			PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION
	);

	private final OccupancyParameter occupancyParameter;
	private final UrgencyParameter urgencyParameter;
	private final PricingWeightResolver weightResolver;

	@Override
	public BlockCalculationResult calculate(PricingContext context) {
		BigDecimal occupancy = occupancyParameter.calculate(context).value();
		BigDecimal urgency = urgencyParameter.calculate(context).value();
		BigDecimal interaction = occupancy.multiply(urgency);
		Map<PricingWeightCode, BigDecimal> weights = weightResolver.resolveNormalized(context.restaurantId(), WEIGHTS);
		BigDecimal value = weights.get(PricingWeightCode.OCCUPANCY_PARAMETER).multiply(occupancy)
				.add(weights.get(PricingWeightCode.URGENCY_PARAMETER).multiply(urgency))
				.add(weights.get(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION).multiply(interaction));
		Map<String, BigDecimal> details = new LinkedHashMap<>();
		details.put("occupancy", occupancy);
		details.put("urgency", urgency);
		details.put("interaction", interaction);
		return new BlockCalculationResult(PricingBlockCode.LOAD, NormalizationUtils.clamp01(value), details);
	}
}
