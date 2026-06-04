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
import my.project.bookingservice.pricing.util.TimeIntervalUtils;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TimeIntervalDemandParameter implements PricingParameter {
	private final PricingHistoryService historyService;
	private final PricingProperties properties;
	private final HistoricalTransitionService transitionService;

	@Override
	public ParameterCalculationResult calculate(PricingContext context) {
		long successfulBookings = historyService.countSuccessfulBookings(context.restaurantId());
		BigDecimal defaultValue = properties.getDefaults().getTimeIntervalDemand();
		if (successfulBookings < properties.getHistory().getMinBookingsForHistory()) {
			return new ParameterCalculationResult(PricingParameterCode.TIME_INTERVAL_DEMAND, defaultValue, PricingValueSource.DEFAULT);
		}
		long max = historyService.maxSuccessfulBookingsByTimeInterval(context.restaurantId());
		if (max <= 0) {
			return new ParameterCalculationResult(PricingParameterCode.TIME_INTERVAL_DEMAND, defaultValue, PricingValueSource.DEFAULT);
		}
		var start = context.visitStart().atZone(BookingTimeUtils.BUSINESS_ZONE).toLocalTime();
		var end = context.visitEnd().atZone(BookingTimeUtils.BUSINESS_ZONE).toLocalTime();
		Map<String, BigDecimal> shares = TimeIntervalUtils.resolveIntervalShares(start, end);
		if (shares.isEmpty()) {
			String interval = TimeIntervalUtils.resolveTimeIntervalCode(start);
			long count = historyService.countSuccessfulBookingsByTimeInterval(context.restaurantId(), interval);
			BigDecimal historicalValue = NormalizationUtils.divide(BigDecimal.valueOf(count), BigDecimal.valueOf(max));
			BigDecimal value = blend(defaultValue, historicalValue, successfulBookings);
			return new ParameterCalculationResult(PricingParameterCode.TIME_INTERVAL_DEMAND, value, PricingValueSource.HISTORICAL);
		}
		BigDecimal historicalValue = shares.entrySet().stream()
				.map(entry -> {
					long count = historyService.countSuccessfulBookingsByTimeInterval(context.restaurantId(), entry.getKey());
					BigDecimal demand = NormalizationUtils.divide(BigDecimal.valueOf(count), BigDecimal.valueOf(max));
					return entry.getValue().multiply(demand);
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal value = blend(defaultValue, historicalValue, successfulBookings);
		return new ParameterCalculationResult(PricingParameterCode.TIME_INTERVAL_DEMAND, value, PricingValueSource.HISTORICAL);
	}

	private BigDecimal blend(BigDecimal defaultValue, BigDecimal historicalValue, long successfulBookings) {
		return transitionService.blend(
				defaultValue,
				historicalValue,
				successfulBookings,
				properties.getHistory().getMinBookingsForHistory(),
				properties.getHistory().getFullBookingsForHistory()
		);
	}
}


