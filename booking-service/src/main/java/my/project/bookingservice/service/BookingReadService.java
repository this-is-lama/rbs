package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.dto.response.TableAvailabilitySlotResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import org.springframework.cache.annotation.Cacheable;
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
	private final BookingMapper mapper;
	private final ManagerAccessService managerAccessService;
	private final RestaurantBookingReadCacheService restaurantBookingReadCacheService;
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

		UUID actorId = AuthUtil.id(auth);
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !managerAccessService.hasManagerAccess(actorId, restId))) {
			log.warn("Доступ к списку бронирований ресторана запрещён, restId={}", restId);
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}

		return restaurantBookingReadCacheService.findRestaurantBookings(restId);
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

		if (AuthUtil.isManager(auth) && !managerAccessService.hasManagerAccess(userId, booking.getRestaurantId())) {
			log.warn("Менеджеру запрещён доступ к бронированию, bookingId={}, restId={}",
					id, booking.getRestaurantId());
			throw new ForbiddenException("booking.forbidden.booking-access");
		}

		return booking;
	}

	@Cacheable(
			cacheNames = "bookingAvailability",
			key = "#restaurantId + ':' + #tableId + ':' + #date"
	)
	public TableAvailabilityResponse getPublicTableAvailability(UUID restaurantId, UUID tableId, LocalDate date) {
		log.info("Получение публичной занятости стола, restaurantId={}, tableId={}, date={}",
				restaurantId, tableId, date);

		List<BookingEntity> bookings = repository
				.findAllByRestaurantIdAndTableIdAndStatusInAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
						restaurantId,
						tableId,
						ACTIVE_STATUSES,
						BookingTimeUtils.dayEnd(date),
						BookingTimeUtils.dayStart(date)
				);

		return createAvailabilityResponse(restaurantId, tableId, date, bookings);
	}

	private TableAvailabilityResponse createAvailabilityResponse(
			UUID restaurantId,
			UUID tableId,
			LocalDate date,
			List<BookingEntity> bookings
	) {
		List<TableAvailabilitySlotResponse> reservedSlots = bookings.stream()
				.map(booking -> new TableAvailabilitySlotResponse(booking.getStartAt(), booking.getEndAt()))
				.toList();
		return new TableAvailabilityResponse(restaurantId, tableId, date, reservedSlots);
	}
}
