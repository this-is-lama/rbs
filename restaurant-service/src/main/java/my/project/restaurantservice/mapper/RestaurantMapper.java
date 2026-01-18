package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.RestaurantDto;
import my.project.restaurantservice.dto.RestaurantInfoDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {
				WorkingHoursMapper.class,
				ContactMapper.class,
				DishMapper.class,
				TableMapper.class
		}
)
public interface RestaurantMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "workingHours", ignore = true)
	@Mapping(target = "contacts", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "tables", ignore = true)
	RestaurantEntity toEntity(RestaurantDto dto);

	RestaurantDto toDto(RestaurantEntity entity);

	List<RestaurantInfoDto> toInfoDto(List<RestaurantEntity> entities);

}

