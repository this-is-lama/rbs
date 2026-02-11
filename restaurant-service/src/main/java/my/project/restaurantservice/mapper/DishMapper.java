package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.client.BookingDishDto;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.entity.DishEntity;
import my.project.restaurantservice.entity.PhotoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {
				PhotoMapper.class
		}
)
public interface DishMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "photos", ignore = true)
	DishEntity toEntity(DishDto dto);

	DishDto toDto(DishEntity entity, List<PhotoEntity> photos);

	@Mapping(target = "photos", ignore = true)
	DishDto toDto(DishEntity entity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "photos", ignore = true)
	void updateEntity(@MappingTarget DishEntity entity, DishDto dto);

	List<DishDto> toDto(List<DishEntity> dishes);

	List<BookingDishDto> toBookingDto(List<DishEntity> entity);
}
