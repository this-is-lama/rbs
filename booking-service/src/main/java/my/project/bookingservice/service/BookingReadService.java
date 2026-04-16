package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingReadService {

	private final BookingRepository repository;
	private final RestaurantServiceClient restaurantClient;
	private final BookingMapper bookingMapper;
	private final BookingHelper bookingHelper;

	public BookingResponse findById(UUID id, Authentication auth) {
		log.info("Получение бронирования по id, bookingId={}", id);
		return bookingMapper.toResponse(findAccessibleEntity(id, auth));
	}

	public List<BookingResponse> findUserBookings(Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Получение списка бронирований пользователя, userId={}", userId);

		List<BookingEntity> bookings = repository.findAllByUserIdOrderByStartAtDesc(userId);
		return bookingMapper.toResponse(bookings);
	}

	public List<BookingResponse> findAllByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка бронирований ресторана, restId={}", restId);

		validateRestaurantReadAccess(restId, auth);

		List<BookingEntity> bookings = repository.findAllByRestaurantId(restId);
		return bookingMapper.toResponse(bookings);
	}

	public TableAvailabilityResponse getPublicTableAvailability(UUID restaurantId, UUID tableId, LocalDate date) {
		log.info("Получение публичной занятости стола, restaurantId={}, tableId={}, date={}",
				restaurantId, tableId, date);

		var bookings = repository
				.findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
						restaurantId,
						tableId,
						BookingStatus.RESERVED,
						bookingHelper.dayEnd(date),
						bookingHelper.dayStart(date)
				);

		return bookingHelper.buildAvailabilityResponse(restaurantId, tableId, date, bookings);
	}

	public BookingEntity findAccessibleEntity(UUID id, Authentication auth) {
		UUID userId = AuthUtil.id(auth);

		Optional<BookingEntity> optional;
		if (AuthUtil.isUser(auth)) {
			log.debug("Поиск бронирования по пользователю, bookingId={}, userId={}", id, userId);
			optional = repository.findByIdAndUserId(id, userId);
		} else {
			log.debug("Поиск бронирования по id для привилегированного пользователя, bookingId={}", id);
			optional = repository.findById(id);
		}

		BookingEntity booking = optional.orElseThrow(() -> {
			log.warn("Бронирование не найдено, bookingId={}", id);
			return new NotFoundException("booking.not-found", id);
		});

		validateBookingAccess(booking, auth);

		return booking;
	}

	private void validateRestaurantReadAccess(UUID restId, Authentication auth) {
		if (AuthUtil.isUser(auth)) {
			log.warn("Доступ к списку бронирований ресторана запрещён, restId={}", restId);
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}

		if (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(restId)) {
			log.warn("Доступ к списку бронирований ресторана запрещён, restId={}", restId);
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}
	}

	private void validateBookingAccess(BookingEntity booking, Authentication auth) {
		if (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(booking.getRestaurantId())) {
			log.warn("Менеджеру запрещён доступ к бронированию, bookingId={}, restId={}",
					booking.getId(), booking.getRestaurantId());
			throw new ForbiddenException("booking.forbidden.booking-access");
		}
	}
}