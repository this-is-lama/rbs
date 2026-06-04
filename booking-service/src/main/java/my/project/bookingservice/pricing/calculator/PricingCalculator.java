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
import my.project.bookingservice.pricing.weights.PricingWeightResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PricingCalculator {
	private static final List<PricingWeightCode> BLOCK_WEIGHTS = List.of(
			PricingWeightCode.LOAD_BLOCK,
			PricingWeightCode.HISTORICAL_DEMAND_BLOCK,
			PricingWeightCode.CALENDAR_CONTEXT_BLOCK
	);

	private final LoadBlock loadBlock;
	private final HistoricalDemandBlock historicalDemandBlock;
	private final CalendarContextBlock calendarContextBlock;
	private final PricingWeightResolver weightResolver;
	private final PricingProperties properties;

	public PricingCalculationResult calculate(PricingContext context) {
		if (!context.hasPreorder()) {
			return zeroResult();
		}
		validatePricingBounds(context);

		BlockCalculationResult load = loadBlock.calculate(context);
		BlockCalculationResult historical = historicalDemandBlock.calculate(context);
		BlockCalculationResult calendar = calendarContextBlock.calculate(context);

		Map<PricingWeightCode, BigDecimal> blockWeights = weightResolver.resolveNormalized(
				context.restaurantId(),
				BLOCK_WEIGHTS
		);

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
		pricingCharge = PricingMathUtils.clampMoney(
				pricingCharge,
				context.restaurantMinPricingCharge(),
				context.restaurantMaxPricingCharge()
		);

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

	private PricingCalculationResult zeroResult() {
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
}
