package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.CreateRestaurantRequest;
import my.project.restaurantservice.dto.RestaurantResponse;
import my.project.restaurantservice.entity.RestaurantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {
				WorkingHoursMapper.class,
				ContactMapper.class,
				DishMapper.class,
		}
)
public interface RestaurantMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "workingHours", ignore = true)
	@Mapping(target = "contacts", ignore = true)
	RestaurantEntity toEntity(CreateRestaurantRequest req);


	RestaurantResponse toResponse(RestaurantEntity entity);


}

