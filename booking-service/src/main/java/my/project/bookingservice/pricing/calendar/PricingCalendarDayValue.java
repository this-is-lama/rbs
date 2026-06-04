package my.project.bookingservice.pricing.calendar;

import my.project.bookingservice.pricing.enums.CalendarDataSource;

import java.time.LocalDate;

public record PricingCalendarDayValue(
		LocalDate date,
		boolean dayOff,
		boolean holiday,
		CalendarDataSource source
) {
}
