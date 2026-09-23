package my.project.restaurantservice.dish.mapper;

import my.project.restaurantservice.dish.dto.DishDetailsDto;
import my.project.restaurantservice.internal.dto.BookingDishDto;
import my.project.restaurantservice.dish.dto.DishDto;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.dish.entity.DishEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DishMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "photos", ignore = true)
	DishEntity toEntity(DishDto dto);

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

	DishDetailsDto toDetailsDto(DishDto dish, List<PhotoDto> photos);
}
