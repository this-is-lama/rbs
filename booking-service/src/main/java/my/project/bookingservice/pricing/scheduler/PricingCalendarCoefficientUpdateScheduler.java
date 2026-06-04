package my.project.bookingservice.pricing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.calendar.PricingCalendarCoefficientUpdateService;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PricingCalendarCoefficientUpdateScheduler {
	private final PricingCalendarCoefficientUpdateService updateService;
	private final BookingRepository bookingRepository;
	private final PricingProperties properties;

	@Scheduled(cron = "${pricing.scheduler.calendar-coefficients-update-cron:0 20 3 * * *}")
	public void updateCalendarCoefficients() {
		LocalDate today = LocalDate.now(BookingTimeUtils.BUSINESS_ZONE);
		LocalDate fromDate = today.minusDays(properties.getHistory().getPeriodDays());
		Instant from = fromDate.atStartOfDay(BookingTimeUtils.BUSINESS_ZONE).toInstant();
		Instant to = Instant.now();

		var restaurantIds = bookingRepository.findRestaurantIdsWithAtLeastSuccessfulBookingsBetween(
				BookingStatus.RESERVED,
				from,
				to,
				properties.getHistory().getMinObservationsForCalendarClass()
		);
		log.info("Плановое обновление календарных коэффициентов динамического сервисного сбора начато: from={}, to={}, restaurants={}",
				from, to, restaurantIds.size());
		restaurantIds.forEach(restaurantId -> {
			int updated = updateService.update(restaurantId);
			log.info("Календарные коэффициенты динамического сервисного сбора обновлены: restaurantId={}, changedCoefficients={}",
					restaurantId, updated);
		});
	}
}


