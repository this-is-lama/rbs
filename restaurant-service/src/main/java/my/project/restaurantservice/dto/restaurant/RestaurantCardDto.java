package my.project.restaurantservice.dto.restaurant;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.dto.photo.PhotoDto;
import my.project.restaurantservice.dto.workinghours.WorkingHoursDto;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestaurantCardDto {

		UUID id;

		String name;

		String category;

		String address;

		Boolean active;

		List<WorkingHoursDto> workingHours;

		PhotoDto bannerPhoto;

}

