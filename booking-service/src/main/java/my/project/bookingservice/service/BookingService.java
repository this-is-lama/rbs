package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
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
	private final RestaurantServiceClient restaurantClient;
	private final BookingMapper bookingMapper;
	private final KafkaProducer kafkaProducer;
	private final BookingHelper bookingHelper;
	private final BookingReadService bookingReadService;

	@Transactional
	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		String email = AuthUtil.email(auth);
		String username = AuthUtil.username(auth);

		log.info("Создание бронирования, userId={}, restaurantId={}, tableId={}",
				userId, req.restaurantId(), req.tableId());

		Map<UUID, Integer> quantities = req.dishesQuantities();

		BookingSnapshotResponse snapshot = restaurantClient.bookingSnapshot(
				req.restaurantId(),
				new BookingSnapshotRequest(req.tableId(), quantities.keySet())
		);

		bookingHelper.validateBookingSnapshot(req, snapshot);

		BookingEntity entity = bookingHelper.buildBookingEntity(req, userId, snapshot, quantities);
		BookingEntity savedEntity = saveBooking(req, entity);

		log.info("Бронирование успешно создано, bookingId={}, userId={}", savedEntity.getId(), userId);

		kafkaProducer.sendBookingCreated(bookingMapper.toEvent(savedEntity, email, username));

		return bookingMapper.toResponse(savedEntity);
	}

	@Transactional
	public void cancel(UUID id, Authentication auth) {
		log.info("Отмена бронирования, bookingId={}", id);

		BookingEntity booking = bookingReadService.findAccessibleEntity(id, auth);
		booking.cancel(Instant.now());

		log.info("Бронирование помечено как отменённое, bookingId={}", id);
	}

	private BookingEntity saveBooking(CreateBookingRequest req, BookingEntity entity) {
		try {
			BookingEntity savedEntity = repository.saveAndFlush(entity);
			log.info("Бронирование сохранено в базе данных, bookingId={}", savedEntity.getId());
			return savedEntity;
		} catch (DataIntegrityViolationException e) {
			log.warn("Конфликт при сохранении бронирования, restaurantId={}, tableId={}",
					req.restaurantId(), req.tableId(), e);
			throw new ConflictException("booking.overlap");
		}
	}
}