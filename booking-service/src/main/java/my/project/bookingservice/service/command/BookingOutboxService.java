package my.project.bookingservice.service.command;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.consistency.outbox.entity.OutboxEventType;
import my.project.bookingservice.consistency.outbox.service.OutboxBookingService;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.mapper.BookingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingOutboxService {

	private final BookingWriteService writeService;
	private final OutboxBookingService outboxService;
	private final BookingMapper mapper;

	public record SavedBooking(BookingResponse booking, UUID outboxId) {}

	@Transactional
	public SavedBooking saveBookingAndOutbox(CreateBookingRequest req, UUID userId, BookingSnapshotResponse snapshot,
											 String email, String username) {
		BookingResponse booking = writeService.save(req, userId, snapshot);

		var event = mapper.toEvent(booking, email, username);
		UUID outboxId = outboxService.save(OutboxEventType.BOOKING_CREATED, booking.id(), event);

		return new SavedBooking(booking, outboxId);
	}

	@Transactional
	public UUID cancelBookingAndOutbox(UUID bookingId, String reason, String email, String username) {
		BookingResponse booking = writeService.cancel(bookingId, reason);

		var event = mapper.toCancelledEvent(booking, email, username, reason);
		return outboxService.save(OutboxEventType.BOOKING_CANCELLED, bookingId, event);
	}
}
