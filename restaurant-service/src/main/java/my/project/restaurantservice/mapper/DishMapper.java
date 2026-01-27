package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.entity.DishEntity;
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

	DishDto toDto(DishEntity dishEntity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "photos", ignore = true)
	void updateEntity(@MappingTarget DishEntity entity, DishDto dto);


	List<DishEntity> toEntity(List<DishDto> dtoList);

	List<DishDto> toDto(List<DishEntity> dishes);
}
