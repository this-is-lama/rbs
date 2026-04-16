package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.events.BookingCreatedEvent;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
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
	@Mapping(target = "totalAmount", ignore = true)

	@Mapping(target = "table", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "dishes", ignore = true)

	@Mapping(target = "tableId", source = "req.tableId")
	@Mapping(target = "restaurantId", source = "req.restaurantId")
	@Mapping(target = "userId", source = "userId")
	BookingEntity toEntity(CreateBookingRequest req, UUID userId);

	BookingResponse toResponse(BookingEntity entity);

	List<BookingResponse> toResponse(List<BookingEntity> entities);

	@Mapping(target = "bookingId", source = "entity.id")
	@Mapping(target = "restaurantName", source = "entity.restaurant.name")
	@Mapping(target = "restaurantDescription", source = "entity.restaurant.description")
	@Mapping(target = "restaurantAddress", source = "entity.restaurant.address")
	@Mapping(target = "tableNumber", source = "entity.table.tableNumber")
	@Mapping(target = "tableDescription", source = "entity.table.description")
	BookingCreatedEvent toEvent(BookingEntity entity, String email, String username);
}