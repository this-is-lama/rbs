package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(
		componentModel = "spring",
		uses = {
				DishMapper.class,
				TableMapper.class,
				RestaurantMapper.class
		}
)
public interface BookingMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "cancelledAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "table", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "tableId", source = "req.tableId")
	@Mapping(target = "restaurantId", source = "req.restaurantId")
	BookingEntity toEntity(CreateBookingRequest req, UUID userId,
						   BookingSnapshotResponse snapshot,
						   @Context Map<UUID, Integer> qty);

	@AfterMapping
	default void fillDetails(@MappingTarget BookingEntity entity,
							 CreateBookingRequest req, UUID userId,
							 BookingSnapshotResponse snapshot,
							 @Context Map<UUID, Integer> qty,
							 DishMapper dishMapper, TableMapper tableMapper,
							 RestaurantMapper restaurantMapper) {

		entity.setTable(tableMapper.toEntity(snapshot.table()));
		entity.setRestaurant(restaurantMapper.toEntity(snapshot.restaurant()));

		var dishes = snapshot.dishes();
		if (dishes != null) {
			dishes.stream().map(dishMapper::toEntity).forEach(d -> {
				d.setQuantity(qty.getOrDefault(d.getDishId(), 1));
				entity.addDish(d);
			});
		}
	}


	BookingResponse toResponse(BookingEntity entity);

	List<BookingResponse> toResponse(List<BookingEntity> entities);

	@Mapping(target = "bookingId", source = "id")
	@Mapping(target = "restaurantName", source = "restaurant.name")
	@Mapping(target = "restaurantDescription", source = "restaurant.description")
	@Mapping(target = "restaurantAddress", source = "restaurant.address")
	@Mapping(target = "tableNumber", source = "table.number")
	@Mapping(target = "tableDescription", source = "table.description")
	BookingCreatedEvent toEvent(BookingEntity entity, String email);

}
