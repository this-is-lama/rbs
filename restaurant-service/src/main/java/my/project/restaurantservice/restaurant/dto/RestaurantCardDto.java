package my.project.restaurantservice.restaurant.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.restaurant.dto.workinghours.WorkingHoursDto;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestaurantCardDto {

		UUID id;

		String name;

		String category;

		String description;

		String address;

		Boolean active;

		WorkingHoursDto workingHour;

		PhotoDto bannerPhoto;

}

