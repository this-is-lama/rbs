package my.project.bookingservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingTimeUtils {
	public static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Moscow");

	public static Instant dayStart(LocalDate date) {
		return date.atStartOfDay(BUSINESS_ZONE).toInstant();
	}

	public static Instant dayEnd(LocalDate date) {
		return date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
	}

	public static LocalDate businessDate(Instant instant) {
		return LocalDate.ofInstant(instant, BUSINESS_ZONE);
	}
}
