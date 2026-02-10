package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.DishDto;
import my.project.bookingservice.dto.response.BookingDishResponse;
import my.project.bookingservice.entity.DishEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DishMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "booking", ignore = true)
	@Mapping(target = "dishId", source = "id")
	@Mapping(target = "quantity", ignore = true)
	DishEntity toEntity(DishDto dto);

	BookingDishResponse toResponse(DishEntity entity);

}
