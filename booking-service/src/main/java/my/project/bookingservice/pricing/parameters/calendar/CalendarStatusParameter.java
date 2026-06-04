package my.project.bookingservice.pricing.parameters.calendar;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.enums.PricingParameterCode;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.parameters.ParameterCalculationResult;
import my.project.bookingservice.pricing.parameters.PricingParameter;
import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.settings.PricingCalendarCoefficientService;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CalendarStatusParameter implements PricingParameter {
	private final CalendarClassifier calendarClassifier;
	private final PricingCalendarCoefficientService settingsService;
	private final PricingProperties properties;
	private final HistoricalTransitionService transitionService;

	@Override
	public ParameterCalculationResult calculate(PricingContext context) {
		CalendarMembership membership = calendarClassifier.classify(context.visitStart().atZone(BookingTimeUtils.BUSINESS_ZONE).toLocalDate());
		BigDecimal numerator = BigDecimal.ZERO;
		BigDecimal denominator = BigDecimal.ZERO;
		PricingValueSource source = PricingValueSource.DEFAULT;
		for (Map.Entry<CalendarDayType, BigDecimal> entry : membership.degrees().entrySet()) {
			BigDecimal degree = entry.getValue() == null ? BigDecimal.ZERO : entry.getValue();
			var coefficientValue = settingsService.getCalendarCoefficientValue(context.restaurantId(), entry.getKey());
			BigDecimal defaultCoefficient = properties.getDefaults().getCalendarCoefficients()
					.getOrDefault(entry.getKey().name(), properties.getDefaults().getNeutralCalendarContext());
			BigDecimal coefficient = coefficientValue.source() == PricingValueSource.HISTORICAL
					? transitionService.blend(
							defaultCoefficient,
							coefficientValue.value(),
							coefficientValue.observationsCount(),
							properties.getHistory().getMinObservationsForCalendarClass(),
							properties.getHistory().getFullObservationsForCalendarClass()
					)
					: defaultCoefficient;
			numerator = numerator.add(degree.multiply(coefficient));
			denominator = denominator.add(degree);
			if (degree.compareTo(BigDecimal.ZERO) > 0
					&& coefficientValue.source() != PricingValueSource.DEFAULT
					&& coefficientValue.observationsCount() >= properties.getHistory().getMinObservationsForCalendarClass()) {
				source = coefficientValue.source();
			}
		}
		BigDecimal value = denominator.compareTo(BigDecimal.ZERO) == 0
				? properties.getDefaults().getNeutralCalendarContext()
				: numerator.divide(denominator, 10, java.math.RoundingMode.HALF_UP);
		return new ParameterCalculationResult(PricingParameterCode.CALENDAR_STATUS, NormalizationUtils.clamp01(value), source);
	}
}


