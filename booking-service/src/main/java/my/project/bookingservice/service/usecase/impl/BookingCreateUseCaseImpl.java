package my.project.bookingservice.service.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.DishEntity;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.mapper.DishMapper;
import my.project.bookingservice.mapper.RestaurantMapper;
import my.project.bookingservice.mapper.TableMapper;
import my.project.bookingservice.service.BookingPersistenceService;
import my.project.bookingservice.service.usecase.BookingCreateUseCase;
import my.project.common.exception.ConflictException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCreateUseCaseImpl implements BookingCreateUseCase {

	private final BookingMapper mapper;
	private final RestaurantMapper restaurantMapper;
	private final DishMapper dishMapper;
	private final TableMapper tableMapper;

	private final BookingPersistenceService persistenceService;

	private final RestaurantServiceClient restaurantClient;
	private final KafkaProducer kafkaProducer;

	@Override
	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		var userId = AuthUtil.id(auth);

		log.info("Создание бронирования, userId={}, restaurantId={}, tableId={}", userId, req.restaurantId(), req.tableId());

		var entity = createBookingEntity(req, userId);
		var savedEntity = persistenceService.save(entity);

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
		return buildBookingEntity(req, userId, bookingSnapshot, quantities);
	}

	private void sendBookingEvent(BookingEntity entity, Authentication auth) {
		var email = AuthUtil.email(auth);
		var username = AuthUtil.username(auth);
		kafkaProducer.sendBookingCreated(mapper.toEvent(entity, email, username));
	}

	private BookingEntity buildBookingEntity(CreateBookingRequest req, UUID userId, BookingSnapshotResponse bookingSnapshot, Map<UUID, Integer> quantities) {
		BookingEntity entity = mapper.toEntity(req, userId);

		entity.setRestaurant(restaurantMapper.toEntity(bookingSnapshot.restaurant()));
		entity.setTable(tableMapper.toEntity(bookingSnapshot.table()));
		if (bookingSnapshot.dishes() != null && !bookingSnapshot.dishes().isEmpty()) {
			bookingSnapshot.dishes().forEach(dishDto -> {
				DishEntity dishEntity = dishMapper.toEntity(dishDto);
				dishEntity.setQuantity(quantities.getOrDefault(dishEntity.getDishId(), 1));
				entity.addDish(dishEntity);
			});
		}
		return entity;
	}
}
