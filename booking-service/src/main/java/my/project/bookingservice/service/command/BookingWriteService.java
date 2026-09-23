package my.project.bookingservice.service.command;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.DishEntity;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.mapper.DishMapper;
import my.project.bookingservice.mapper.RestaurantMapper;
import my.project.bookingservice.mapper.TableMapper;
import my.project.bookingservice.repository.BookingRepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingWriteService {

	@Value("${pricing.charge-coefficient}")
	private double chargeCoefficient;

	private final BookingRepositoryService repositoryService;

	private final BookingMapper mapper;
	private final RestaurantMapper restaurantMapper;
	private final DishMapper dishMapper;
	private final TableMapper tableMapper;

	@Transactional
	public BookingResponse save(CreateBookingRequest req, UUID userId, BookingSnapshotResponse snapshot) {
		BookingEntity entity = buildBookingEntity(req, userId, snapshot);
		return mapper.toResponse(repositoryService.save(entity));
	}

	@Transactional
	public BookingResponse cancel(UUID bookingId, String reason) {
		BookingEntity booking = repositoryService.getById(bookingId);
		booking.cancel(Instant.now(), reason);
		return mapper.toResponse(booking);
	}

	private BookingEntity buildBookingEntity(CreateBookingRequest req, UUID userId, BookingSnapshotResponse snapshot) {
		BookingEntity entity = mapper.toEntity(req, userId);

		entity.setRestaurant(restaurantMapper.toEntity(snapshot.restaurant()));
		entity.setTable(tableMapper.toEntity(snapshot.table()));

		Map<UUID, Integer> quantities = req.dishesQuantities();
		BigDecimal preorderAmount = BigDecimal.ZERO;
		if (snapshot.dishes() != null && !snapshot.dishes().isEmpty()) {
			for (var dishDto : snapshot.dishes()) {
				DishEntity dishEntity = dishMapper.toEntity(dishDto);
				dishEntity.setQuantity(quantities.getOrDefault(dishEntity.getDishId(), 1));
				entity.addDish(dishEntity);
				preorderAmount = preorderAmount.add(dishDto.price());
			}
		}
		entity.setPricing(preorderAmount, chargeCoefficient);

		return entity;
	}
}
