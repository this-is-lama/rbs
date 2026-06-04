package my.project.bookingservice.pricing.history;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;

public record PricingHistoryAggregate(
		UUID restaurantId,
		long totalSuccessfulBookings,
		Map<DayOfWeek, Long> bookingsByWeekday,
		Map<String, Long> bookingsByTimeInterval,
		Map<UUID, Long> bookingsByTable
) {
	public long countByWeekday(DayOfWeek dayOfWeek) {
		return bookingsByWeekday.getOrDefault(dayOfWeek, 0L);
	}

	public long maxByWeekday() {
		return bookingsByWeekday.values().stream().mapToLong(Long::longValue).max().orElse(0L);
	}

	public long countByTimeInterval(String interval) {
		return bookingsByTimeInterval.getOrDefault(interval, 0L);
	}

	public long maxByTimeInterval() {
		return bookingsByTimeInterval.values().stream().mapToLong(Long::longValue).max().orElse(0L);
	}

	public long countByTable(UUID tableId) {
		return bookingsByTable.getOrDefault(tableId, 0L);
	}

	public long maxByTable() {
		return bookingsByTable.values().stream().mapToLong(Long::longValue).max().orElse(0L);
	}
}
