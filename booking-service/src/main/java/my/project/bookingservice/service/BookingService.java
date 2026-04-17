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
import my.project.common.security.AuthUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

	private final BookingRepository repository;
	private final BookingMapper mapper;
	private final BookingReadService readService;
	private final BookingHelper helper;

	private final RestaurantServiceClient restaurantClient;
	private final KafkaProducer kafkaProducer;


	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var userId = AuthUtil.id(auth);

		log.info("Создание бронирования, userId={}, restaurantId={}, tableId={}", userId, req.restaurantId(), req.tableId());

		var entity = createBookingEntity(req, userId);
		var savedEntity = save(entity);

		log.info("Бронирование успешно создано, bookingId={}, userId={}", savedEntity.getId(), userId);
		sendBookingEvent(savedEntity, auth);

		return mapper.toResponse(savedEntity);
	}

	private BookingEntity createBookingEntity(CreateBookingRequest req, UUID userId) {
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
		return helper.buildBookingEntity(req, userId, bookingSnapshot, quantities);
	}

	private void sendBookingEvent(BookingEntity entity, Authentication auth) {
		var email = AuthUtil.email(auth);
		var username = AuthUtil.username(auth);
		kafkaProducer.sendBookingCreated(mapper.toEvent(entity, email, username));
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


	@Transactional
	public void cancel(UUID id, Authentication auth) {
		var now = Instant.now();

		log.info("Отмена бронирования, bookingId={}", id);

		BookingEntity booking = readService.findByAuth(id, auth);
		booking.cancel(now);

		log.info("Бронирование помечено как отменённое, bookingId={}", id);
	}

}