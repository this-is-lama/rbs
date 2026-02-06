package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.DishDto;
import my.project.bookingservice.dto.TableDto;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository repository;
	private final RestaurantServiceClient restaurantClient;
	private final BookingMapper bookingMapper;

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		// позже сделаю миграции и EXCLUDE USING gist для диапазонов времени (tsrange)
		if (repository.existsOverlapping(req.tableId(), BookingStatus.RESERVED, req.startAt(), req.endAt())) {
			throw new ConflictException("booking.overlap");
		}

		TableDto table = restaurantClient.checkTable(req.restaurantId(), req.tableId());
		if (table.capacity() < req.guests()) {
			throw new ConflictException("booking.table.guest-more-capacity");
		}

		var userId = AuthUtil.id(auth);
		BookingEntity entity;

		if (req.dishes() == null || req.dishes().isEmpty()) {
			entity = bookingMapper.toEntity(req, userId);
		} else {
			var dishesSnapshot = restaurantClient.findAllByIds(req.restaurantId(), req.getDishesIds());
			var dishesById = dishesSnapshot.stream().collect(Collectors.toMap(DishDto::id, d -> d));
			entity = bookingMapper.toEntity(req, userId, dishesById);
		}

		return bookingMapper.toResponse(repository.save(entity));
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
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !restaurantClient.managerAccess(restId))) {
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

	private BookingEntity findByAuth(UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);

		BookingEntity booking;
		if (AuthUtil.isUser(auth)) {
			booking = repository.findByIdAndUserId(id, userId)
					.orElseThrow(() -> new NotFoundException("booking.not-found", id));
		} else {
			booking = repository.findById(id)
					.orElseThrow(() -> new NotFoundException("booking.not-found", id));
		}

		if (AuthUtil.isManager(auth) && !restaurantClient.managerAccess(booking.getRestaurantId())) {
			throw new ForbiddenException("booking.forbidden.booking-access");
		}
		return booking;
	}

}
