package my.project.bookingservice.pricing.scheduler;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.history.PricingHistorySnapshotBuildService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PricingHistorySnapshotScheduler {
	private final PricingHistorySnapshotBuildService buildService;

	@Scheduled(cron = "${pricing.scheduler.history-snapshot-cron:0 30 2 * * *}")
	public void buildSnapshots() {
		buildService.buildYesterdaySnapshots();
	}
}
