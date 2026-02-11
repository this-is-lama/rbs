package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository repository;
	private final RestaurantServiceClient restaurantClient;
	private final BookingMapper bookingMapper;

	private final KafkaProducer kafkaProducer;

	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var userId = AuthUtil.id(auth);
		var email = AuthUtil.email(auth);
		Map<UUID, Integer> quantities = req.dishesQuantities();
		var bookingSnapshot = restaurantClient.bookingSnapshot(req.restaurantId(),
				new BookingSnapshotRequest(req.tableId(), quantities.keySet()));

		if (bookingSnapshot.table().capacity() < req.guests()) {
			throw new ConflictException("booking.table.guest-more-capacity");
		}

		BookingEntity entity = bookingMapper.toEntity(req, userId, bookingSnapshot, quantities);
		entity = save(entity);

		kafkaProducer.sendBookingCreated(bookingMapper.toEvent(entity, email));

		return bookingMapper.toResponse(entity);
	}

	@Transactional
	public BookingEntity save(BookingEntity booking) {
		try {
			return repository.saveAndFlush(booking);
		} catch (DataIntegrityViolationException e) {
			throw new ConflictException("booking.overlap");
		}
	}

	@Transactional(readOnly = true)
	public BookingResponse findById(UUID id, Authentication auth) {
		var booking = findByAuth(id, auth);
		return bookingMapper.toResponse(booking);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> findUserBookings(Authentication auth) {
		var userId = AuthUtil.id(auth);
		List<BookingEntity> bookings = repository.findAllByUserIdOrderByStartAtDesc(userId);
		return bookingMapper.toResponse(bookings);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> findAllByRestaurantId(UUID restId, Authentication auth) {
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(restId))) {
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}
		var bookings = repository.findAllByRestaurantId(restId);
		return bookingMapper.toResponse(bookings);
	}

	@Transactional
	public void cancel(UUID id, Authentication auth) {
		var now = Instant.now();

		BookingEntity booking = findByAuth(id, auth);
		booking.cancel(now);
	}

	@Transactional(readOnly = true)
	public BookingEntity findByAuth(UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);

		Optional<BookingEntity> optional;
		if (AuthUtil.isUser(auth)) {
			optional = repository.findByIdAndUserId(id, userId);
		} else {
			optional = repository.findById(id);
		}
		var booking = optional.orElseThrow(() -> new NotFoundException("booking.not-found", id));

		if (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(booking.getRestaurant().getRestaurantId())) {
			throw new ForbiddenException("booking.forbidden.booking-access");
		}
		return booking;
	}

}
