package my.project.restaurantservice.restaurant.mapper;

import my.project.restaurantservice.dish.dto.DishDetailsDto;
import my.project.restaurantservice.restaurant.entity.ContactEntity;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.entity.WorkingHoursEntity;
import my.project.restaurantservice.internal.dto.BookingRestaurantDto;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDetailsDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.dto.RestaurantCardDto;
import my.project.restaurantservice.table.dto.TableDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {
				WorkingHoursMapper.class,
				ContactMapper.class
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
	@Mapping(target = "managers", ignore = true)
	RestaurantEntity toEntity(RestaurantDto dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "workingHours", ignore = true)
	@Mapping(target = "contacts", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "tables", ignore = true)
	@Mapping(target = "photos", ignore = true)
	@Mapping(target = "managers", ignore = true)
	void updateEntity(@MappingTarget RestaurantEntity entity, RestaurantDto dto);

	@Mapping(target = "workingHours", source = "wh")
	@Mapping(target = "contacts", source = "contacts")
	RestaurantDto toDto(RestaurantEntity entity,
						List<WorkingHoursEntity> wh,
						List<ContactEntity> contacts);

	RestaurantDetailsDto toDetailsDto(RestaurantDto restaurant,
									  List<DishDetailsDto> dishes,
									  List<TableDto> tables,
									  List<PhotoDto> photos);

	@Mapping(target = "id", source = "restaurant.id")
	@Mapping(target = "name", source = "restaurant.name")
	@Mapping(target = "category", source = "restaurant.category")
	@Mapping(target = "description", source = "restaurant.description")
	@Mapping(target = "address", source = "restaurant.address")
	@Mapping(target = "active", source = "restaurant.active")
	@Mapping(target = "bannerPhoto", source = "bannerPhoto")
	@Mapping(target = "workingHour", source = "workingHour")
	RestaurantCardDto toCardDto(RestaurantEntity restaurant, PhotoDto bannerPhoto, WorkingHoursEntity workingHour);

	BookingRestaurantDto toBookingDto(RestaurantEntity entity);

}
