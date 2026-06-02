package my.project.bookingservice.pricing.calculator;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.blocks.BlockCalculationResult;
import my.project.bookingservice.pricing.blocks.calendar.CalendarContextBlock;
import my.project.bookingservice.pricing.blocks.historical.HistoricalDemandBlock;
import my.project.bookingservice.pricing.blocks.load.LoadBlock;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.exception.PricingCalculationException;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.util.PricingMathUtils;
import my.project.bookingservice.pricing.weights.PricingWeightProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PricingCalculator {
	private final LoadBlock loadBlock;
	private final HistoricalDemandBlock historicalDemandBlock;
	private final CalendarContextBlock calendarContextBlock;
	private final PricingWeightProvider weightProvider;
	private final PricingProperties properties;

	public PricingCalculationResult calculate(PricingContext context) {
		if (!context.hasPreorder()) {
			return new PricingCalculationResult(
					PricingMathUtils.money(BigDecimal.ZERO),
					PricingMathUtils.money(BigDecimal.ZERO),
					PricingMathUtils.money(BigDecimal.ZERO),
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					Instant.now()
			);
		}
		validatePricingBounds(context);

		BlockCalculationResult load = loadBlock.calculate(context);
		BlockCalculationResult historical = historicalDemandBlock.calculate(context);
		BlockCalculationResult calendar = calendarContextBlock.calculate(context);

		Map<PricingWeightCode, BigDecimal> blockWeights = normalizedBlockWeights(context);

		BigDecimal demandIndex = blockWeights.get(PricingWeightCode.LOAD_BLOCK).multiply(load.value())
				.add(blockWeights.get(PricingWeightCode.HISTORICAL_DEMAND_BLOCK).multiply(historical.value()))
				.add(blockWeights.get(PricingWeightCode.CALENDAR_CONTEXT_BLOCK).multiply(calendar.value()));
		demandIndex = NormalizationUtils.clamp01(demandIndex);

		BigDecimal pricingCharge = PricingMathUtils.sigmoidPricingCharge(
				demandIndex,
				context.restaurantMinPricingCharge(),
				context.restaurantMaxPricingCharge(),
				properties.getSigmoid().getK(),
				properties.getSigmoid().getCenter()
		);
		pricingCharge = PricingMathUtils.clampMoney(pricingCharge, context.restaurantMinPricingCharge(), context.restaurantMaxPricingCharge());

		BigDecimal totalAmount = PricingMathUtils.money(context.preorderAmount().add(pricingCharge));
		return new PricingCalculationResult(
				PricingMathUtils.money(context.preorderAmount()),
				PricingMathUtils.money(pricingCharge),
				totalAmount,
				demandIndex,
				load.value(),
				historical.value(),
				calendar.value(),
				Instant.now()
		);
	}

	private void validatePricingBounds(PricingContext context) {
		BigDecimal min = context.restaurantMinPricingCharge();
		BigDecimal max = context.restaurantMaxPricingCharge();
		if (min == null || max == null || min.compareTo(max) > 0) {
			throw new PricingCalculationException("Invalid restaurant pricing charge bounds");
		}
		if (min.compareTo(properties.getSystemLimits().getMinPricingCharge()) < 0
				|| max.compareTo(properties.getSystemLimits().getMaxPricingCharge()) > 0) {
			throw new PricingCalculationException("Restaurant pricing charge bounds are outside system limits");
		}
	}

	private Map<PricingWeightCode, BigDecimal> normalizedBlockWeights(PricingContext context) {
		Map<PricingWeightCode, BigDecimal> source = new EnumMap<>(PricingWeightCode.class);
		source.put(PricingWeightCode.LOAD_BLOCK, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.LOAD_BLOCK));
		source.put(PricingWeightCode.HISTORICAL_DEMAND_BLOCK, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.HISTORICAL_DEMAND_BLOCK));
		source.put(PricingWeightCode.CALENDAR_CONTEXT_BLOCK, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.CALENDAR_CONTEXT_BLOCK));
		if (positiveSum(source).compareTo(BigDecimal.ZERO) <= 0) {
			source = new EnumMap<>(PricingWeightCode.class);
			source.put(PricingWeightCode.LOAD_BLOCK, defaultWeight(PricingWeightCode.LOAD_BLOCK));
			source.put(PricingWeightCode.HISTORICAL_DEMAND_BLOCK, defaultWeight(PricingWeightCode.HISTORICAL_DEMAND_BLOCK));
			source.put(PricingWeightCode.CALENDAR_CONTEXT_BLOCK, defaultWeight(PricingWeightCode.CALENDAR_CONTEXT_BLOCK));
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

