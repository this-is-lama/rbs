package my.project.bookingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.client.UserServiceClient;
import my.project.bookingservice.dto.client.UserDto;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingReadService;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingReadServiceImpl implements BookingReadService {

	private final BookingRepository repository;
	private final BookingMapper mapper;

	private final RestaurantServiceClient restaurantClient;
	private final UserServiceClient userClient;

	public BookingResponse findById(UUID id, Authentication auth) {
		log.info("Получение бронирования по id, bookingId={}", id);
		var booking = findByAuth(id, auth);
		return mapper.toResponse(booking);
	}

	public List<BookingResponse> findUserBookings(Authentication auth) {
		var userId = AuthUtil.id(auth);
		log.info("Получение списка бронирований пользователя, userId={}", userId);

		List<BookingEntity> bookings = repository.findAllByUserIdOrderByCreatedAtDesc(userId);
		return mapper.toResponse(bookings);
	}

	public List<ManagerBookingResponse> findAllByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка бронирований ресторана, restId={}", restId);

		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(restId))) {
			log.warn("Доступ к списку бронирований ресторана запрещён, restId={}", restId);
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}

		log.info("Загрузка списка бронирований ресторана из БД, restaurantId={}", restId);
		List<BookingEntity> bookings = repository.findAllByRestaurantIdOrderByCreatedAtDesc(restId);
		Set<UUID> userIds = bookings.stream()
				.map(BookingEntity::getUserId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		var usersMap = userClient.getUsersByIds(userIds)
				.stream()
				.collect(Collectors.toMap(
						UserDto::id,
						Function.identity(),
						(oldValue, newValue) -> newValue
				));

		return bookings.stream()
				.map(booking -> mapper.toManagerResponse(booking, usersMap.get(booking.getUserId())))
				.toList();
	}


	public BookingEntity findByAuth(UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);

		Optional<BookingEntity> optional;
		if (AuthUtil.isUser(auth)) {
			log.debug("Поиск бронирования по пользователю, bookingId={}, userId={}", id, userId);
			optional = repository.findByIdAndUserId(id, userId);
		} else {
			log.debug("Поиск бронирования по id для привилегированного пользователя, bookingId={}", id);
			optional = repository.findById(id);
		}

		var booking = optional.orElseThrow(() -> {
			log.warn("Бронирование не найдено, bookingId={}", id);
			return new NotFoundException("booking.not-found", id);
		});

		if (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(booking.getRestaurant().getRestaurantId())) {
			log.warn("Менеджеру запрещён доступ к бронированию, bookingId={}, restId={}",
					id, booking.getRestaurant().getRestaurantId());
			throw new ForbiddenException("booking.forbidden.booking-access");
		}

		return booking;
	}

	public List<BookingEntity> findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(UUID restId, UUID tableId,
																															  BookingStatus status,
																															  Instant from, Instant to) {
		return repository.findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(restId, tableId, status, from, to);
	}

}