package my.project.bookingservice.service.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.UserServiceClient;
import my.project.bookingservice.dto.client.UserDto;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.service.BookingPersistenceService;
import my.project.bookingservice.service.BookingReadService;
import my.project.bookingservice.service.usecase.BookingCancelUseCase;
import my.project.common.exception.ValidationException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCancelUseCaseImpl implements BookingCancelUseCase {

	private final BookingMapper mapper;

	private final BookingReadService readService;
	private final BookingPersistenceService bookingPersistenceService;

	private final UserServiceClient userServiceClient;
	private final KafkaProducer kafkaProducer;

	@Override
	public void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth) {
		log.info("Отмена бронирования, bookingId={}", bookingId);

		var booking = getBooking(bookingId, auth);
		if (booking == null) return;

		UserDto user = userServiceClient.getUserById(booking.getUserId());

		boolean cancelledByManagerOrAdmin = AuthUtil.isManager(auth) || AuthUtil.isAdmin(auth);
		String reason = checkReason(bookingId, request, cancelledByManagerOrAdmin);

		BookingEntity saved = bookingPersistenceService.cancel(booking, reason);
		log.info("Бронирование помечено как отменённое, bookingId={}", bookingId);

		sendBookingEvent(cancelledByManagerOrAdmin, saved, user, reason);
	}

	private BookingEntity getBooking(UUID bookingId, Authentication auth) {
		BookingEntity booking = readService.findByAuth(bookingId, auth);
		if (booking.isCancelled()) {
			log.info("Бронирование уже было отменено ранее, bookingId={}", bookingId);
			return null;
		}
		return booking;
	}

	private String checkReason(UUID bookingId, CancelBookingRequest request, boolean cancelledByManagerOrAdmin) {
		String reason = request == null ? null : request.reason();
		if (cancelledByManagerOrAdmin && (reason == null || reason.isBlank())) {
			log.warn("Причина отмены бронирования менеджером или администратором не указана, bookingId={}", bookingId);
			throw new ValidationException("booking.cancel.reason-required");
		}
		return reason;
	}

	private void sendBookingEvent(boolean cancelledByManagerOrAdmin,
								  BookingEntity booking,
								  UserDto user, String reason) {
		if (cancelledByManagerOrAdmin) {
			String username = user.surname() + " " + user.name();
			var event = mapper.toCancelledEvent(booking, user.email(), username, reason);
			kafkaProducer.sendBookingCancelled(event);
		}
	}
}
