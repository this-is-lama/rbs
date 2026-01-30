package my.project.restaurantservice.dto.restaurant;

import my.project.restaurantservice.dto.workinghours.WorkingHoursDto;

import java.util.List;
import java.util.UUID;

public record RestaurantInfoDto(
		UUID id,

		String name,

		String category,

		String address,

		Boolean active,

		List<WorkingHoursDto> workingHours
) {}

