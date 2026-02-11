package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.client.BookingRestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantCardDto;
import my.project.restaurantservice.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {
				WorkingHoursMapper.class,
				ContactMapper.class,
				DishMapper.class,
				TableMapper.class,
				PhotoMapper.class,
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
	@Mapping(target = "photos", ignore = true)
	RestaurantEntity toEntity(RestaurantDto dto);

	RestaurantDto toDto(RestaurantEntity entity);

	RestaurantDto toDto(RestaurantEntity entity,
						List<WorkingHoursEntity> wh,
						List<ContactEntity> contacts,
						List<DishEntity> dishes,
						List<TableEntity> tables,
						List<PhotoEntity> photos);


	RestaurantCardDto toCardDto(RestaurantEntity restaurant, PhotoEntity bannerPhoto, WorkingHoursEntity workingHours);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "workingHours", ignore = true)
	@Mapping(target = "contacts", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "tables", ignore = true)
	@Mapping(target = "photos", ignore = true)
	void updateEntity(@MappingTarget RestaurantEntity entity, RestaurantDto dto);

	BookingRestaurantDto toBookingDto(RestaurantEntity entity);

}

