package my.project.bookingservice.pricing.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingPricingHistoryService implements PricingHistoryService {
	private final PricingHistoryAggregateService aggregateService;

	@Override
	public long countSuccessfulBookings(UUID restaurantId) {
		return aggregateService.getAggregate(restaurantId).totalSuccessfulBookings();
	}

	@Override
	public long countSuccessfulBookingsByWeekday(UUID restaurantId, DayOfWeek dayOfWeek) {
		return aggregateService.getAggregate(restaurantId).countByWeekday(dayOfWeek);
	}

	@Override
	public long maxSuccessfulBookingsByWeekday(UUID restaurantId) {
		return aggregateService.getAggregate(restaurantId).maxByWeekday();
	}

	@Override
	public long countSuccessfulBookingsByTimeInterval(UUID restaurantId, String timeIntervalCode) {
		return aggregateService.getAggregate(restaurantId).countByTimeInterval(timeIntervalCode);
	}

	@Override
	public long maxSuccessfulBookingsByTimeInterval(UUID restaurantId) {
		return aggregateService.getAggregate(restaurantId).maxByTimeInterval();
	}

	@Override
	public long countSuccessfulBookingsByTable(UUID restaurantId, UUID tableId) {
		return aggregateService.getAggregate(restaurantId).countByTable(tableId);
	}

	@Override
	public long maxSuccessfulBookingsByTable(UUID restaurantId) {
		return aggregateService.getAggregate(restaurantId).maxByTable();
	}
}
