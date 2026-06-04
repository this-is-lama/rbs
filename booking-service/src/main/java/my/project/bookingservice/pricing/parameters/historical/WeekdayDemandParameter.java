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
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component
@RequiredArgsConstructor
public class WeekdayDemandParameter implements PricingParameter {
	private final PricingHistoryService historyService;
	private final PricingProperties properties;
	private final HistoricalTransitionService transitionService;

	@Override
	public ParameterCalculationResult calculate(PricingContext context) {
		long successfulBookings = historyService.countSuccessfulBookings(context.restaurantId());
		BigDecimal defaultValue = properties.getDefaults().getWeekdayDemand();
		if (successfulBookings < properties.getHistory().getMinBookingsForHistory()) {
			return new ParameterCalculationResult(PricingParameterCode.WEEKDAY_DEMAND, properties.getDefaults().getWeekdayDemand(), PricingValueSource.DEFAULT);
		}
		DayOfWeek day = context.visitStart().atZone(BookingTimeUtils.BUSINESS_ZONE).getDayOfWeek();
		long count = historyService.countSuccessfulBookingsByWeekday(context.restaurantId(), day);
		long max = historyService.maxSuccessfulBookingsByWeekday(context.restaurantId());
		if (max <= 0) {
			return new ParameterCalculationResult(PricingParameterCode.WEEKDAY_DEMAND, defaultValue, PricingValueSource.DEFAULT);
		}
		BigDecimal historicalValue = NormalizationUtils.divide(BigDecimal.valueOf(count), BigDecimal.valueOf(max));
		BigDecimal value = transitionService.blend(
				defaultValue,
				historicalValue,
				successfulBookings,
				properties.getHistory().getMinBookingsForHistory(),
				properties.getHistory().getFullBookingsForHistory()
		);
		return new ParameterCalculationResult(PricingParameterCode.WEEKDAY_DEMAND, value, PricingValueSource.HISTORICAL);
	}
}


