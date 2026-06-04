package my.project.bookingservice.pricing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.weights.PricingWeightCalculationService;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PricingWeightsUpdateScheduler {
	private final PricingWeightCalculationService calculationService;
	private final BookingRepository bookingRepository;
	private final PricingProperties properties;

	@Scheduled(cron = "${pricing.scheduler.weights-update-cron:0 0 3 * * *}")
	public void updateWeights() {
		LocalDate today = LocalDate.now(BookingTimeUtils.BUSINESS_ZONE);
		LocalDate fromDate = today.minusDays(properties.getHistory().getPeriodDays());
		Instant from = fromDate.atStartOfDay(BookingTimeUtils.BUSINESS_ZONE).toInstant();
		Instant to = Instant.now();

		var restaurantIds = bookingRepository.findRestaurantIdsWithAtLeastSuccessfulBookingsBetween(
				BookingStatus.RESERVED,
				from,
				to,
				properties.getHistory().getMinBookingsForWeightHistory()
		);
		log.info("Плановое обновление весов динамического сервисного сбора начато: from={}, to={}, restaurants={}",
				from, to, restaurantIds.size());

		int successCount = 0;
		int errorCount = 0;
		for (var restaurantId : restaurantIds) {
			try {
				var result = calculationService.calculateRecommendedWeights(restaurantId);
				successCount++;
				log.info("Веса динамического сервисного сбора обновлены: restaurantId={}, changedWeights={}",
						restaurantId, result.items().size());
			} catch (Exception ex) {
				errorCount++;
				log.error("Ошибка при обновлении весов динамического сервисного сбора: restaurantId={}",
						restaurantId, ex);
			}
		}

		log.info("Плановое обновление весов динамического сервисного сбора завершено: всего={}, успешно={}, сОшибкой={}",
				restaurantIds.size(), successCount, errorCount);
	}
}
