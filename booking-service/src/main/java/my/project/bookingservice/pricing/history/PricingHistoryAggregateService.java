package my.project.bookingservice.pricing.history;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.TimeIntervalUtils;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingHistoryAggregateService {
	private final BookingRepository bookingRepository;
	private final PricingProperties properties;

	@Cacheable(cacheNames = "pricingHistoryAggregates", key = "#restaurantId")
	public PricingHistoryAggregate getAggregate(UUID restaurantId) {
		LocalDate today = LocalDate.now(BookingTimeUtils.BUSINESS_ZONE);
		LocalDate fromDate = today.minusDays(properties.getHistory().getPeriodDays());
		Instant from = fromDate.atStartOfDay(BookingTimeUtils.BUSINESS_ZONE).toInstant();
		Instant to = Instant.now();

		long total = bookingRepository.countByRestaurantIdAndStatusAndStartAtGreaterThanEqualAndStartAtLessThan(
				restaurantId,
				BookingStatus.RESERVED,
				from,
				to
		);

		Map<DayOfWeek, Long> byWeekday = loadWeekdayCounts(restaurantId, from, to);
		Map<String, Long> byTimeInterval = loadTimeIntervalCounts(restaurantId, from, to);
		Map<UUID, Long> byTable = loadTableCounts(restaurantId, from, to);

		return new PricingHistoryAggregate(restaurantId, total, byWeekday, byTimeInterval, byTable);
	}

	private Map<DayOfWeek, Long> loadWeekdayCounts(UUID restaurantId, Instant from, Instant to) {
		return bookingRepository.countSuccessfulBookingsByWeekday(
						restaurantId,
						BookingStatus.RESERVED.name(),
						from,
						to
				)
				.stream()
				.collect(Collectors.toMap(
						row -> DayOfWeek.of(Integer.parseInt(row.getKey())),
						row -> row.getCount() == null ? 0L : row.getCount()
				));
	}

	private Map<UUID, Long> loadTableCounts(UUID restaurantId, Instant from, Instant to) {
		return bookingRepository.countSuccessfulBookingsByTable(
						restaurantId,
						BookingStatus.RESERVED.name(),
						from,
						to
				)
				.stream()
				.collect(Collectors.toMap(
						row -> UUID.fromString(row.getKey()),
						row -> row.getCount() == null ? 0L : row.getCount()
				));
	}

	private Map<String, Long> loadTimeIntervalCounts(UUID restaurantId, Instant from, Instant to) {
		return bookingRepository.findSuccessfulBookingStartTimes(
						restaurantId,
						BookingStatus.RESERVED,
						from,
						to
				)
				.stream()
				.collect(Collectors.groupingBy(
						row -> TimeIntervalUtils.resolveTimeIntervalCode(
								row.getStartAt().atZone(BookingTimeUtils.BUSINESS_ZONE).toLocalTime()
						),
						Collectors.counting()
				));
	}
}
