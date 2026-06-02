package my.project.bookingservice.service;

import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.client.UserServiceClient;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.pricing.context.PricingContextFactory;
import my.project.bookingservice.pricing.offer.PricingOfferUsageService;
import my.project.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceStatusFlowTest {
	private final BookingRepository repository = mock(BookingRepository.class);
	private final BookingMapper mapper = mock(BookingMapper.class);
	private final BookingReadService readService = mock(BookingReadService.class);
	private final BookingService service = new BookingService(
			repository,
			mapper,
			readService,
			mock(BookingHelper.class),
			mock(RestaurantServiceClient.class),
			mock(UserServiceClient.class),
			mock(KafkaProducer.class),
			mock(PricingContextFactory.class),
			mock(PricingOfferUsageService.class)
	);

	@Test
	void newBookingDefaultsToReserved() {
		BookingEntity booking = booking(null, Instant.now().plusSeconds(3600));

		booking.prePersist();

		assertThat(booking.getStatus()).isEqualTo(BookingStatus.RESERVED);
	}

	@Test
	void cancelMovesReservedBookingToCancelled() {
		BookingEntity booking = booking(BookingStatus.RESERVED, Instant.now().plusSeconds(3600));
		when(readService.findByAuth(booking.getId(), null)).thenReturn(booking);

		service.cancel(booking.getId(), new CancelBookingRequest("plans changed"), null);

		assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
		assertThat(booking.getCancellationReason()).isEqualTo("plans changed");
		verify(repository).saveAndFlush(booking);
	}

	@Test
	void cancelCancelledBookingDoesNotSaveAgain() {
		BookingEntity booking = booking(BookingStatus.CANCELLED, Instant.now().minusSeconds(60));
		when(readService.findByAuth(booking.getId(), null)).thenReturn(booking);

		service.cancel(booking.getId(), new CancelBookingRequest("already cancelled"), null);

		verify(repository, never()).saveAndFlush(booking);
	}

	private BookingEntity booking(BookingStatus status, Instant endAt) {
		BookingEntity booking = new BookingEntity();
		booking.setId(UUID.randomUUID());
		booking.setRestaurantId(UUID.randomUUID());
		booking.setUserId(UUID.randomUUID());
		booking.setTableId(UUID.randomUUID());
		booking.setStartAt(endAt.minusSeconds(3600));
		booking.setEndAt(endAt);
		booking.setStatus(status);
		return booking;
	}
}
