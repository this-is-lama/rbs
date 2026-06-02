package my.project.bookingservice.pricing.parameters.calendar;

import my.project.bookingservice.pricing.enums.CalendarDayType;

import java.math.BigDecimal;
import java.util.Map;

public record CalendarMembership(Map<CalendarDayType, BigDecimal> degrees) {
}

