package my.project.bookingservice.pricing.parameters.calendar;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultCalendarClassifier implements CalendarClassifier {
	private final PricingProperties properties;
	private final IsDayOffCalendarService isDayOffCalendarService;

	@Override
	public CalendarMembership classify(LocalDate date) {
		Map<CalendarDayType, BigDecimal> degrees = new EnumMap<>(CalendarDayType.class);
		boolean dayOff = isDayOffCalendarService.isDayOff(date);
		boolean holiday = isDayOffCalendarService.isHoliday(date) || properties.getCalendar().getHolidays().contains(date);
		boolean peakHoliday = properties.getCalendar().getPeakHolidays().contains(date);
		degrees.put(CalendarDayType.WORKDAY, !dayOff && !holiday ? BigDecimal.ONE : BigDecimal.ZERO);
		degrees.put(CalendarDayType.WEEKEND, dayOff ? BigDecimal.ONE : BigDecimal.ZERO);
		degrees.put(CalendarDayType.HOLIDAY, holiday ? BigDecimal.ONE : BigDecimal.ZERO);
		degrees.put(CalendarDayType.PEAK_HOLIDAY, peakHoliday ? BigDecimal.ONE : BigDecimal.ZERO);
		return new CalendarMembership(degrees);
	}
}

