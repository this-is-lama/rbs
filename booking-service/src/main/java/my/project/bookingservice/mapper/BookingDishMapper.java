package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.DishDto;
import my.project.bookingservice.dto.request.BookingDishCreateRequest;
import my.project.bookingservice.dto.response.BookingDishResponse;
import my.project.bookingservice.entity.BookingDishEntity;
import org.mapstruct.*;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookingDishMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "booking", ignore = true)
	@Mapping(target = "dishName", ignore = true)
	@Mapping(target = "price", ignore = true)
	BookingDishEntity toEntity(BookingDishCreateRequest req, @Context Map<UUID, DishDto> dishesSnapshot);

	BookingDishResponse toResponse(BookingDishEntity entity);

	@AfterMapping
	default void fillSnapshot(BookingDishCreateRequest req,
							  @MappingTarget BookingDishEntity entity,
							  @Context Map<UUID, DishDto> dishesSnapshot) {
		DishDto dish = dishesSnapshot.get(req.dishId());
		entity.setDishName(dish.name());
		entity.setPrice(dish.price());
	}
}
