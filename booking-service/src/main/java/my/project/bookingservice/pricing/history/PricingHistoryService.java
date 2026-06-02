package my.project.bookingservice.pricing.history;

import java.time.DayOfWeek;
import java.util.UUID;

public interface PricingHistoryService {
	long countSuccessfulBookings(UUID restaurantId);
	long countSuccessfulBookingsByWeekday(UUID restaurantId, DayOfWeek dayOfWeek);
	long maxSuccessfulBookingsByWeekday(UUID restaurantId);
	long countSuccessfulBookingsByTimeInterval(UUID restaurantId, String timeIntervalCode);
	long maxSuccessfulBookingsByTimeInterval(UUID restaurantId);
	long countSuccessfulBookingsByTable(UUID restaurantId, UUID tableId);
	long maxSuccessfulBookingsByTable(UUID restaurantId);
}

