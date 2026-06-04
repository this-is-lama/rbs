package my.project.bookingservice.pricing.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.enums.CalendarDataSource;
import my.project.bookingservice.pricing.persistence.entity.PricingCalendarDayEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingCalendarDayRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingCalendarDayService {
	private final PricingCalendarDayRepository repository;

	@Cacheable(
			cacheNames = "pricingCalendarDays",
			key = "#date",
			unless = "#result.source().name() == 'FALLBACK'"
	)
	public PricingCalendarDayValue getDay(LocalDate date) {
		return repository.findByCalendarDate(date)
				.map(this::toValue)
				.orElseGet(() -> {
					log.warn("Календарные данные отсутствуют в БД, применяется резервное правило, date={}", date);
					return fallback(date);
				});
	}

	private PricingCalendarDayValue toValue(PricingCalendarDayEntity entity) {
		return new PricingCalendarDayValue(
				entity.getCalendarDate(),
				entity.isDayOff(),
				entity.isHoliday(),
				entity.getSource()
		);
	}

	private PricingCalendarDayValue fallback(LocalDate date) {
		DayOfWeek day = date.getDayOfWeek();
		boolean weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
		return new PricingCalendarDayValue(date, weekend, false, CalendarDataSource.FALLBACK);
	}
}
