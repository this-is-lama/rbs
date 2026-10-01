package my.project.bookingservice.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantGateway;
import my.project.bookingservice.client.UserGateway;
import my.project.bookingservice.consistency.outbox.service.OutboxProcessorService;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.UserDto;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.service.query.BookingDetailsService;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ValidationException;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCommandService {

	private final BookingWriteService writeService;
	private final BookingOutboxService outboxService;
	private final OutboxProcessorService processorService;
	private final BookingDetailsService detailsService;

	private final RestaurantGateway restaurantGateway;
	private final UserGateway userGateway;

	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var snapshot = restaurantGateway.bookingSnapshot(
				req.restaurantId(),
				new BookingSnapshotRequest(req.tableId(), req.dishesQuantities().keySet())
		);

		if (snapshot.table().capacity() < req.guests()) {
			log.warn("Невозможно создать бронирование: количество гостей превышает вместимость стола, restaurantId={}, tableId={}", req.restaurantId(), req.tableId());
			throw new ConflictException("booking.table.guest-more-capacity");
		}

		var saved = outboxService.saveBookingAndOutbox(
				req, AuthUtil.id(auth), snapshot, AuthUtil.email(auth), AuthUtil.username(auth));

		processorService.process(saved.outboxId());

		return saved.booking();
	}

	public void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth) {
		var booking = detailsService.findById(bookingId, auth);
		if (booking.status() == BookingStatus.CANCELLED) {
			log.info("Бронирование уже было отменено ранее, bookingId={}", bookingId);
			return;
		}

		UserDto user = userGateway.getUserById(booking.userId());

		boolean cancelledByManagerOrAdmin = AuthUtil.isManager(auth) || AuthUtil.isAdmin(auth);
		String reason = checkReason(bookingId, request, cancelledByManagerOrAdmin);

		if (cancelledByManagerOrAdmin) {
			String username = user.surname() + " " + user.name();
			UUID outboxId = outboxService.cancelBookingAndOutbox(bookingId, reason, user.email(), username);
			processorService.process(outboxId);
		} else {
			writeService.cancel(bookingId, reason);
		}
	}

	private String checkReason(UUID bookingId, CancelBookingRequest request, boolean cancelledByManagerOrAdmin) {
		String reason = request == null ? null : request.reason();
		if (cancelledByManagerOrAdmin && (reason == null || reason.isBlank())) {
			log.warn("Причина отмены бронирования менеджером или администратором не указана, bookingId={}", bookingId);
			throw new ValidationException("booking.cancel.reason-required");
		}
		return reason;
	}
}