package my.project.bookingservice.pricing.parameters.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsDayOffCalendarService {
	private static final String WORKDAY = "0";
	private static final String DAY_OFF = "1";
	private static final String HOLIDAY = "8";

	private final IsDayOffClient client;
	private final PricingProperties properties;
	private final Map<String, String> responseCache = new ConcurrentHashMap<>();

	public boolean isDayOff(LocalDate date) {
		try {
			String value = load(date, 0);
			return DAY_OFF.equals(value);
		} catch (RuntimeException ex) {
			log.warn("isDayOff API failed, falling back to local weekend rules, date={}", date, ex);
			DayOfWeek day = date.getDayOfWeek();
			return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
		}
	}

	public boolean isHoliday(LocalDate date) {
		try {
			String value = load(date, 1);
			return HOLIDAY.equals(value);
		} catch (RuntimeException ex) {
			log.warn("isDayOff holiday API failed, falling back to pricing.calendar.holidays, date={}", date, ex);
			return properties.getCalendar().getHolidays().contains(date);
		}
	}

	private String load(LocalDate date, int holiday) {
		String cacheKey = date + ":" + holiday + ":" + properties.getCalendar().getCountryCode();
		String value = responseCache.computeIfAbsent(cacheKey, ignored -> client.getData(
						date.getYear(),
						date.getMonthValue(),
						date.getDayOfMonth(),
						properties.getCalendar().getCountryCode(),
						holiday
				)
		);
		if (value == null) {
			throw new IllegalStateException("isDayOff returned empty response");
		}
		value = value.trim();
		if (!WORKDAY.equals(value) && !DAY_OFF.equals(value) && !HOLIDAY.equals(value)) {
			throw new IllegalStateException("Unexpected isDayOff response: " + value);
		}
		return value;
	}
}

