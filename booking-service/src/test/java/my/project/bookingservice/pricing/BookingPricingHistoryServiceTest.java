package my.project.bookingservice.pricing;

import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.history.BookingPricingHistoryService;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.TimeIntervalUtils;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingHelper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingPricingHistoryServiceTest {
	@Test
	void historicalCountsUseOnlyPastReservedBookings() {
		UUID restaurantId = UUID.randomUUID();
		UUID tableA = UUID.randomUUID();
		UUID tableB = UUID.randomUUID();
		LocalDate today = LocalDate.now(BookingHelper.BUSINESS_ZONE);
		Instant pastLunch = today.minusDays(1).atTime(12, 0).atZone(BookingHelper.BUSINESS_ZONE).toInstant();
		Instant futureLunch = today.plusDays(1).atTime(12, 0).atZone(BookingHelper.BUSINESS_ZONE).toInstant();
		Instant oldLunch = today.minusDays(120).atTime(12, 0).atZone(BookingHelper.BUSINESS_ZONE).toInstant();

		List<BookingEntity> allBookings = List.of(
				booking(restaurantId, tableA, BookingStatus.RESERVED, pastLunch),
				booking(restaurantId, tableB, BookingStatus.RESERVED, pastLunch.plusSeconds(1800)),
				booking(restaurantId, tableA, BookingStatus.RESERVED, futureLunch),
				booking(restaurantId, tableA, BookingStatus.CANCELLED, pastLunch.plusSeconds(3600)),
				booking(restaurantId, tableA, BookingStatus.RESERVED, oldLunch)
		);

		BookingRepository repository = mock(BookingRepository.class);
		when(repository.findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqualAndStartAtLessThan(
				eq(restaurantId),
				eq(BookingStatus.RESERVED),
				any(),
				any()
		)).thenAnswer(invocation -> {
			Instant from = invocation.getArgument(2);
			Instant to = invocation.getArgument(3);
			return allBookings.stream()
					.filter(booking -> booking.getRestaurantId().equals(restaurantId))
					.filter(booking -> booking.getStatus() == BookingStatus.RESERVED)
					.filter(booking -> !booking.getStartAt().isBefore(from))
					.filter(booking -> booking.getStartAt().isBefore(to))
					.toList();
		});

		PricingProperties properties = new PricingProperties();
		properties.getHistory().setPeriodDays(90);
		BookingPricingHistoryService service = new BookingPricingHistoryService(repository, properties);

		String interval = TimeIntervalUtils.resolveTimeIntervalCode(
				pastLunch.atZone(BookingHelper.BUSINESS_ZONE).toLocalTime()
		);

		assertThat(service.countSuccessfulBookings(restaurantId)).isEqualTo(2);
		assertThat(service.countSuccessfulBookingsByWeekday(
				restaurantId,
				pastLunch.atZone(BookingHelper.BUSINESS_ZONE).getDayOfWeek()
		)).isEqualTo(2);
		assertThat(service.countSuccessfulBookingsByTimeInterval(restaurantId, interval)).isEqualTo(2);
		assertThat(service.countSuccessfulBookingsByTable(restaurantId, tableA)).isEqualTo(1);
		assertThat(service.maxSuccessfulBookingsByTable(restaurantId)).isEqualTo(1);
		verify(repository, never()).findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqual(any(), any(), any());
	}

	private BookingEntity booking(UUID restaurantId, UUID tableId, BookingStatus status, Instant startAt) {
		BookingEntity booking = new BookingEntity();
		booking.setId(UUID.randomUUID());
		booking.setRestaurantId(restaurantId);
		booking.setTableId(tableId);
		booking.setStatus(status);
		booking.setStartAt(startAt);
		booking.setEndAt(startAt.plusSeconds(3600));
		return booking;
	}
}
