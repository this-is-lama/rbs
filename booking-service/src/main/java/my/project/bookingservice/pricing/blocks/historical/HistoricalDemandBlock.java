package my.project.bookingservice.pricing.blocks.historical;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.blocks.BlockCalculationResult;
import my.project.bookingservice.pricing.blocks.PricingBlock;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingBlockCode;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.parameters.historical.TableDemandParameter;
import my.project.bookingservice.pricing.parameters.historical.TimeIntervalDemandParameter;
import my.project.bookingservice.pricing.parameters.historical.WeekdayDemandParameter;
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
public class HistoricalDemandBlock implements PricingBlock {
	private final WeekdayDemandParameter weekdayDemandParameter;
	private final TimeIntervalDemandParameter timeIntervalDemandParameter;
	private final TableDemandParameter tableDemandParameter;
	private final PricingWeightProvider weightProvider;
	private final PricingProperties properties;

	@Override
	public BlockCalculationResult calculate(PricingContext context) {
		BigDecimal weekday = weekdayDemandParameter.calculate(context).value();
		BigDecimal timeInterval = timeIntervalDemandParameter.calculate(context).value();
		BigDecimal table = tableDemandParameter.calculate(context).value();
		Map<PricingWeightCode, BigDecimal> weights = normalizedWeights(context);
		BigDecimal value = weights.get(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER).multiply(weekday)
				.add(weights.get(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER).multiply(timeInterval))
				.add(weights.get(PricingWeightCode.TABLE_DEMAND_PARAMETER).multiply(table));
		Map<String, BigDecimal> details = new LinkedHashMap<>();
		details.put("weekdayDemand", weekday);
		details.put("timeIntervalDemand", timeInterval);
		details.put("tableDemand", table);
		return new BlockCalculationResult(PricingBlockCode.HISTORICAL_DEMAND, NormalizationUtils.clamp01(value), details);
	}

	private Map<PricingWeightCode, BigDecimal> normalizedWeights(PricingContext context) {
		Map<PricingWeightCode, BigDecimal> source = new EnumMap<>(PricingWeightCode.class);
		source.put(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.WEEKDAY_DEMAND_PARAMETER));
		source.put(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER));
		source.put(PricingWeightCode.TABLE_DEMAND_PARAMETER, weightProvider.getWeight(context.restaurantId(), PricingWeightCode.TABLE_DEMAND_PARAMETER));
		if (positiveSum(source).compareTo(BigDecimal.ZERO) <= 0) {
			source = new EnumMap<>(PricingWeightCode.class);
			source.put(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER, defaultWeight(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER));
			source.put(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER, defaultWeight(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER));
			source.put(PricingWeightCode.TABLE_DEMAND_PARAMETER, defaultWeight(PricingWeightCode.TABLE_DEMAND_PARAMETER));
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

