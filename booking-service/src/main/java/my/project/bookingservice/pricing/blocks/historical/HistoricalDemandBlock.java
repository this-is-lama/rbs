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
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.weights.PricingWeightResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HistoricalDemandBlock implements PricingBlock {
	private static final List<PricingWeightCode> WEIGHTS = List.of(
			PricingWeightCode.WEEKDAY_DEMAND_PARAMETER,
			PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER,
			PricingWeightCode.TABLE_DEMAND_PARAMETER
	);

	private final WeekdayDemandParameter weekdayDemandParameter;
	private final TimeIntervalDemandParameter timeIntervalDemandParameter;
	private final TableDemandParameter tableDemandParameter;
	private final PricingWeightResolver weightResolver;

	@Override
	public BlockCalculationResult calculate(PricingContext context) {
		BigDecimal weekday = weekdayDemandParameter.calculate(context).value();
		BigDecimal timeInterval = timeIntervalDemandParameter.calculate(context).value();
		BigDecimal table = tableDemandParameter.calculate(context).value();
		Map<PricingWeightCode, BigDecimal> weights = weightResolver.resolveNormalized(context.restaurantId(), WEIGHTS);
		BigDecimal value = weights.get(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER).multiply(weekday)
				.add(weights.get(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER).multiply(timeInterval))
				.add(weights.get(PricingWeightCode.TABLE_DEMAND_PARAMETER).multiply(table));
		Map<String, BigDecimal> details = new LinkedHashMap<>();
		details.put("weekdayDemand", weekday);
		details.put("timeIntervalDemand", timeInterval);
		details.put("tableDemand", table);
		return new BlockCalculationResult(PricingBlockCode.HISTORICAL_DEMAND, NormalizationUtils.clamp01(value), details);
	}
}
