package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.dto.response.TableAvailabilitySlotResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.DishEntity;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.mapper.DishMapper;
import my.project.bookingservice.mapper.RestaurantMapper;
import my.project.bookingservice.mapper.TableMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingHelper {

	public static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Moscow");

	private final BookingMapper bookingMapper;
	private final RestaurantMapper restaurantMapper;
	private final DishMapper dishMapper;
	private final TableMapper tableMapper;


	public BookingEntity buildBookingEntity(CreateBookingRequest req, UUID userId, BookingSnapshotResponse bookingSnapshot, Map<UUID, Integer> quantities) {
		BookingEntity entity = bookingMapper.toEntity(req, userId);

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

	public Instant dayStart(LocalDate date) {
		return date.atStartOfDay(BUSINESS_ZONE).toInstant();
	}

	public Instant dayEnd(LocalDate date) {
		return date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
	}

	public TableAvailabilityResponse buildAvailabilityResponse(UUID restaurantId,
															   UUID tableId,
															   LocalDate date,
															   List<BookingEntity> bookings) {
		List<TableAvailabilitySlotResponse> reservedSlots = bookings.stream()
				.map(booking -> new TableAvailabilitySlotResponse(
						booking.getStartAt(),
						booking.getEndAt()
				))
				.toList();

		return new TableAvailabilityResponse(restaurantId, tableId, date, reservedSlots);
	}
}
