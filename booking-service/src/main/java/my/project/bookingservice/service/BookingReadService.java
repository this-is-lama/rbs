package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.client.UserServiceClient;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.BookingUserResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingReadService {

	private final BookingRepository repository;
	private final BookingMapper mapper;
	private final BookingHelper helper;

	private final RestaurantServiceClient restaurantClient;
	private final UserServiceClient userServiceClient;
	private static final List<BookingStatus> ACTIVE_STATUSES = List.of(BookingStatus.RESERVED);

	public BookingResponse findById(UUID id, Authentication auth) {
		log.info("Получение бронирования по id, bookingId={}", id);
		BookingEntity booking = findByAuth(id, auth);
		return mapper.toResponse(booking);
	}

	public List<BookingResponse> findUserBookings(Authentication auth) {
		UUID userId = AuthUtil.id(auth);
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

		List<BookingEntity> bookings = repository.findAllByRestaurantIdOrderByCreatedAtDesc(restId);

		Set<UUID> userIds = bookings.stream()
				.map(BookingEntity::getUserId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, BookingUserResponse> usersById = userServiceClient.getBriefs(userIds).stream()
				.map(mapper::toBookingUserResponse)
				.collect(Collectors.toMap(BookingUserResponse::id, Function.identity()));

		return bookings.stream()
				.map(booking -> mapper.toManagerResponse(booking, usersById.get(booking.getUserId())))
				.toList();
	}

	public BookingEntity findByAuth(UUID id, Authentication auth) {
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

		if (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(booking.getRestaurantId())) {
			log.warn("Менеджеру запрещён доступ к бронированию, bookingId={}, restId={}",
					id, booking.getRestaurantId());
			throw new ForbiddenException("booking.forbidden.booking-access");
		}

		return booking;
	}

	public TableAvailabilityResponse getPublicTableAvailability(UUID restaurantId, UUID tableId, LocalDate date) {
		log.info("Получение публичной занятости стола, restaurantId={}, tableId={}, date={}",
				restaurantId, tableId, date);

		List<BookingEntity> bookings = repository
				.findAllByRestaurantIdAndTableIdAndStatusInAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
						restaurantId,
						tableId,
						ACTIVE_STATUSES,
						helper.dayEnd(date),
						helper.dayStart(date)
				);

		return helper.buildAvailabilityResponse(restaurantId, tableId, date, bookings);
	}
}
