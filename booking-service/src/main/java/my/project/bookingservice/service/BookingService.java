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
		var username = AuthUtil.username(auth);

		log.info("Создание бронирования, userId={}, restaurantId={}, tableId={}",
				userId, req.restaurantId(), req.tableId());

		Map<UUID, Integer> quantities = req.dishesQuantities();

		var bookingSnapshot = restaurantClient.bookingSnapshot(
				req.restaurantId(),
				new BookingSnapshotRequest(req.tableId(), quantities.keySet())
		);

		if (bookingSnapshot.table().capacity() < req.guests()) {
			log.warn("Невозможно создать бронирование: количество гостей превышает вместимость стола, restaurantId={}, tableId={}",
					req.restaurantId(), req.tableId());
			throw new ConflictException("booking.table.guest-more-capacity");
		}

		BookingEntity entity = bookingMapper.toEntity(req, userId, bookingSnapshot, quantities);
		entity = save(entity);

		log.info("Бронирование успешно создано, bookingId={}, userId={}", entity.getId(), userId);

		kafkaProducer.sendBookingCreated(bookingMapper.toEvent(entity, email, username));

		return bookingMapper.toResponse(entity);
	}

	@Transactional
	public BookingEntity save(BookingEntity booking) {
		try {
			BookingEntity saved = repository.saveAndFlush(booking);
			log.info("Бронирование сохранено в базе данных, bookingId={}", saved.getId());
			return saved;
		} catch (DataIntegrityViolationException e) {
			log.warn("Конфликт при сохранении бронирования: пересечение по времени, tableId={}", booking.getTableId(), e);
			throw new ConflictException("booking.overlap");
		}
	}

	@Transactional(readOnly = true)
	public BookingResponse findById(UUID id, Authentication auth) {
		log.info("Получение бронирования по id, bookingId={}", id);
		var booking = findByAuth(id, auth);
		return bookingMapper.toResponse(booking);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> findUserBookings(Authentication auth) {
		var userId = AuthUtil.id(auth);
		log.info("Получение списка бронирований пользователя, userId={}", userId);

		List<BookingEntity> bookings = repository.findAllByUserIdOrderByStartAtDesc(userId);
		return bookingMapper.toResponse(bookings);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> findAllByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка бронирований ресторана, restId={}", restId);

		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !restaurantClient.hasManagerAccess(restId))) {
			log.warn("Доступ к списку бронирований ресторана запрещён, restId={}", restId);
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}

		var bookings = repository.findAllByRestaurantId(restId);
		return bookingMapper.toResponse(bookings);
	}

	@Transactional
	public void cancel(UUID id, Authentication auth) {
		var now = Instant.now();

		log.info("Отмена бронирования, bookingId={}", id);

		BookingEntity booking = findByAuth(id, auth);
		booking.cancel(now);

		log.info("Бронирование помечено как отменённое, bookingId={}", id);
	}

	@Transactional(readOnly = true)
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
}