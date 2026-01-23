package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.DishDto;
import my.project.restaurantservice.entity.DishEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {
				DishPhotoMapper.class,
		}
)
public interface DishMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "photos", ignore = true)
	DishEntity toEntity(DishDto dto);

	DishDto toDto(DishEntity dishEntity);


	List<DishEntity> toEntity(List<DishDto> dtoList);

	List<DishDto> toDto(List<DishEntity> dishes);
}
