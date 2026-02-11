package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.client.BookingRestaurantDto;
import my.project.bookingservice.dto.response.RestaurantResponse;
import my.project.bookingservice.entity.RestaurantEntity;
import org.mapstruct.Mapping;

public interface RestaurantMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "booking", ignore = true)
	@Mapping(target = "restaurantId", source = "id")
	RestaurantEntity toEntity(BookingRestaurantDto dto);

	RestaurantResponse toResponse(RestaurantEntity entity);
}
