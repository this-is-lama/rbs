package my.project.restaurantservice.restaurant.mapper;

import my.project.restaurantservice.contact.dto.ContactDto;
import my.project.restaurantservice.dish.dto.DishDetailsDto;
import my.project.restaurantservice.internal.dto.BookingRestaurantDto;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.restaurant.dto.RestaurantCardDto;
import my.project.restaurantservice.restaurant.dto.RestaurantCreateRequest;
import my.project.restaurantservice.restaurant.dto.RestaurantDetailsDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.dto.RestaurantUpdateDto;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.util.CategoryNormalizer;
import my.project.restaurantservice.table.dto.TableDto;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RestaurantMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "workingHours", ignore = true)
	@Mapping(target = "contacts", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "tables", ignore = true)
	@Mapping(target = "photos", ignore = true)
	@Mapping(target = "managers", ignore = true)
	@Mapping(target = "category", source = "category", qualifiedByName = "normalizeCategory")
	RestaurantEntity toEntity(RestaurantCreateRequest dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "workingHours", ignore = true)
	@Mapping(target = "contacts", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "tables", ignore = true)
	@Mapping(target = "photos", ignore = true)
	@Mapping(target = "managers", ignore = true)
	@Mapping(target = "category", source = "category", qualifiedByName = "normalizeCategory")
	void updateEntity(@MappingTarget RestaurantEntity entity, RestaurantUpdateDto dto);

	RestaurantDto toDto(RestaurantEntity entity);

	RestaurantDetailsDto toDetailsDto(RestaurantDto restaurant,
									  List<ContactDto> contacts,
									  List<WorkingHoursDto> workingHours,
									  List<DishDetailsDto> dishes,
									  List<TableDto> tables,
									  List<PhotoDto> photos);

	@Mapping(target = "id", source = "restaurant.id")
	@Mapping(target = "name", source = "restaurant.name")
	@Mapping(target = "category", source = "restaurant.category")
	@Mapping(target = "description", source = "restaurant.description")
	@Mapping(target = "city", source = "restaurant.city")
	@Mapping(target = "street", source = "restaurant.street")
	@Mapping(target = "house", source = "restaurant.house")
	@Mapping(target = "status", source = "restaurant.status")
	@Mapping(target = "bannerPhoto", source = "bannerPhoto")
	@Mapping(target = "workingHour", source = "workingHour")
	RestaurantCardDto toCardDto(RestaurantEntity restaurant, PhotoDto bannerPhoto, WorkingHoursDto workingHour);

	// booking-service хранит адрес одной строкой
	@Mapping(target = "address", source = "fullAddress")
	BookingRestaurantDto toBookingDto(RestaurantEntity entity);

	@Named("normalizeCategory")
	default String normalizeCategory(String category) {
		return CategoryNormalizer.normalize(category);
	}

}
