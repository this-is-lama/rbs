package my.project.bookingservice.pricing.parameters.historical;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingParameterCode;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import my.project.bookingservice.pricing.parameters.ParameterCalculationResult;
import my.project.bookingservice.pricing.parameters.PricingParameter;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TableDemandParameter implements PricingParameter {
	private final PricingHistoryService historyService;
	private final PricingProperties properties;
	private final HistoricalTransitionService transitionService;

	@Override
	public ParameterCalculationResult calculate(PricingContext context) {
		long successfulBookings = historyService.countSuccessfulBookings(context.restaurantId());
		BigDecimal defaultValue = properties.getDefaults().getTableDemand();
		if (successfulBookings < properties.getHistory().getMinBookingsForTableHistory()) {
			return new ParameterCalculationResult(PricingParameterCode.TABLE_DEMAND, defaultValue, PricingValueSource.DEFAULT);
		}
		long tableBookings = historyService.countSuccessfulBookingsByTable(context.restaurantId(), context.tableId());
		long max = historyService.maxSuccessfulBookingsByTable(context.restaurantId());
		if (max <= 0) {
			return new ParameterCalculationResult(PricingParameterCode.TABLE_DEMAND, defaultValue, PricingValueSource.DEFAULT);
		}
		BigDecimal historicalValue = NormalizationUtils.divide(BigDecimal.valueOf(tableBookings), BigDecimal.valueOf(max));
		BigDecimal value = transitionService.blend(
				defaultValue,
				historicalValue,
				successfulBookings,
				properties.getHistory().getMinBookingsForTableHistory(),
				properties.getHistory().getFullBookingsForTableHistory()
		);
		return new ParameterCalculationResult(PricingParameterCode.TABLE_DEMAND, value, PricingValueSource.HISTORICAL);
	}
}

