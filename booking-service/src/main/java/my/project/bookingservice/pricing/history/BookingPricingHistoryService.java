package my.project.bookingservice.pricing.history;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.TimeIntervalUtils;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingHelper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingPricingHistoryService implements PricingHistoryService {
	private final BookingRepository bookingRepository;
	private final PricingProperties properties;

	@Override
	public long countSuccessfulBookings(UUID restaurantId) {
		return bookings(restaurantId).size();
	}

	@Override
	public long countSuccessfulBookingsByWeekday(UUID restaurantId, DayOfWeek dayOfWeek) {
		return bookings(restaurantId).stream()
				.filter(booking -> booking.getStartAt().atZone(BookingHelper.BUSINESS_ZONE).getDayOfWeek() == dayOfWeek)
				.count();
	}

	@Override
	public long maxSuccessfulBookingsByWeekday(UUID restaurantId) {
		return maxGrouped(restaurantId, booking -> booking.getStartAt().atZone(BookingHelper.BUSINESS_ZONE).getDayOfWeek());
	}

	@Override
	public long countSuccessfulBookingsByTimeInterval(UUID restaurantId, String timeIntervalCode) {
		return bookings(restaurantId).stream()
				.filter(booking -> interval(booking).equals(timeIntervalCode))
				.count();
	}

	@Override
	public long maxSuccessfulBookingsByTimeInterval(UUID restaurantId) {
		return maxGrouped(restaurantId, this::interval);
	}

	@Override
	public long countSuccessfulBookingsByTable(UUID restaurantId, UUID tableId) {
		return bookings(restaurantId).stream()
				.filter(booking -> tableId.equals(booking.getTableId()))
				.count();
	}

	@Override
	public long maxSuccessfulBookingsByTable(UUID restaurantId) {
		return maxGrouped(restaurantId, BookingEntity::getTableId);
	}

	private <T> long maxGrouped(UUID restaurantId, Function<BookingEntity, T> classifier) {
		return bookings(restaurantId).stream()
				.collect(Collectors.groupingBy(classifier, Collectors.counting()))
				.values()
				.stream()
				.mapToLong(Long::longValue)
				.max()
				.orElse(0);
	}

	private String interval(BookingEntity booking) {
		return TimeIntervalUtils.resolveTimeIntervalCode(
				booking.getStartAt().atZone(BookingHelper.BUSINESS_ZONE).toLocalTime()
		);
	}

	private List<BookingEntity> bookings(UUID restaurantId) {
		LocalDate today = LocalDate.now(BookingHelper.BUSINESS_ZONE);
		LocalDate fromDate = today.minusDays(properties.getHistory().getPeriodDays());
		Instant from = fromDate.atStartOfDay(BookingHelper.BUSINESS_ZONE).toInstant();
		Instant to = Instant.now();

		return bookingRepository.findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqualAndStartAtLessThan(
				restaurantId,
				BookingStatus.RESERVED,
				from,
				to
		);
	}
}

