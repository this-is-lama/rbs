package my.project.bookingservice.pricing.parameters.calendar;

import java.time.LocalDate;

public interface CalendarClassifier {
	CalendarMembership classify(LocalDate date);
}

