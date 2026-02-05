package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.DishDto;
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

	@Transactional
	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var userId = AuthUtil.id(auth);
		var dishesSnapshot = restaurantClient.findAllByIds(req.restaurantId(),req.getDishesIds());

		var dishesById = dishesSnapshot.stream().collect(Collectors.toMap(DishDto::id, d -> d));
		BookingEntity entity = bookingMapper.toEntity(req, userId, dishesById);

		if (!repository.existsOverlapping(entity.getTableId(), BookingStatus.RESERVED, entity.getStartAt(), entity.getEndAt())) {
			entity = repository.save(entity);
			return bookingMapper.toResponse(entity);
		}
		throw new ConflictException("");
	}

	@Transactional(readOnly = true)
	public BookingResponse findById(UUID id) {
		BookingEntity booking = repository.findById(id)
				.orElseThrow(() -> new NotFoundException(id.toString()));
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
			throw new ForbiddenException("");
		}
		var bookings = repository.findAllByRestaurantId(restId);
		return bookingMapper.toResponse(bookings);
	}


	@Transactional
	public void cancel(UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);
		var now = Instant.now();

		BookingEntity booking;

		if (AuthUtil.isUser(auth)) {
			booking = repository.findByIdAndUserId(id, userId)
					.orElseThrow(() -> new NotFoundException(id.toString()));
		} else {
			booking = repository.findById(id)
					.orElseThrow(() -> new NotFoundException(id.toString()));
		}

		if (AuthUtil.isManager(auth)) {
			if (restaurantClient.managerAccess(booking.getRestaurantId())) {
				booking.cancel(now);
			} else {
				throw new ForbiddenException("");
			}
		} else {
			booking.cancel(now);
		}

	}

}
