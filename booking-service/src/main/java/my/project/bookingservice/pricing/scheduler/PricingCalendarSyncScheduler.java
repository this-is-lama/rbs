package my.project.bookingservice.pricing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.calendar.PricingCalendarSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PricingCalendarSyncScheduler {
	private final PricingCalendarSyncService syncService;

	@Scheduled(cron = "${pricing.scheduler.calendar-sync-cron:0 0 4 */14 * *}")
	public void syncCalendar() {
		log.info("Плановая синхронизация производственного календаря запущена");
		syncService.syncNextTwoWeeks();
	}
}
