package my.project.bookingservice.pricing.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.enums.CalendarDataSource;
import my.project.bookingservice.pricing.parameters.calendar.IsDayOffClient;
import my.project.bookingservice.pricing.persistence.entity.PricingCalendarDayEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingCalendarDayRepository;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingCalendarSyncService {
	private static final String COUNTRY_CODE = "ru";
	private static final String WORKDAY = "0";
	private static final String DAY_OFF = "1";
	private static final String HOLIDAY = "8";
	private static final int DAYS_TO_SYNC = 14;

	private final IsDayOffClient isDayOffClient;
	private final PricingCalendarDayRepository repository;
	private final PricingCalendarDayCacheService cacheService;

	@Transactional
	public void syncNextTwoWeeks() {
		LocalDate from = LocalDate.now(BookingTimeUtils.BUSINESS_ZONE);
		LocalDate to = from.plusDays(DAYS_TO_SYNC - 1);
		log.info("Синхронизация производственного календаря начата: from={}, to={}", from, to);

		int successCount = 0;
		int errorCount = 0;
		for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
			try {
				syncDate(date);
				successCount++;
			} catch (RuntimeException ex) {
				errorCount++;
				log.warn("Не удалось синхронизировать календарный день через API, date={}", date, ex);
			}
		}

		log.info("Синхронизация производственного календаря завершена: from={}, to={}, успешно={}, сОшибкой={}",
				from, to, successCount, errorCount);
	}

	private void syncDate(LocalDate date) {
		String dayOffValue = load(date, 0);
		String holidayValue = load(date, 1);
		boolean dayOff = DAY_OFF.equals(dayOffValue);
		boolean holiday = HOLIDAY.equals(holidayValue);

		PricingCalendarDayEntity entity = repository.findByCalendarDate(date)
				.orElseGet(PricingCalendarDayEntity::new);
		entity.setCalendarDate(date);
		entity.setDayOff(dayOff);
		entity.setHoliday(holiday);
		entity.setSource(CalendarDataSource.API);
		entity.setLoadedAt(Instant.now());

		PricingCalendarDayEntity saved = repository.save(entity);
		cacheService.put(new PricingCalendarDayValue(
				saved.getCalendarDate(),
				saved.isDayOff(),
				saved.isHoliday(),
				saved.getSource()
		));

		log.info("Календарный день синхронизирован: date={}, dayOff={}, holiday={}", date, dayOff, holiday);
	}

	private String load(LocalDate date, int holiday) {
		String value = isDayOffClient.getData(
				date.getYear(),
				date.getMonthValue(),
				date.getDayOfMonth(),
				COUNTRY_CODE,
				holiday
		);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Пустой ответ API производственного календаря");
		}

		value = value.trim();
		if (!WORKDAY.equals(value) && !DAY_OFF.equals(value) && !HOLIDAY.equals(value)) {
			throw new IllegalStateException("Неожиданный ответ API производственного календаря: " + value);
		}
		return value;
	}
}
