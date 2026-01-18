package my.project.restaurantservice.dto;

import java.util.List;
import java.util.UUID;

public record RestaurantInfoDto(
		UUID id,
		String name,
		String category,
		String address,
		Boolean isActive,
		List<WorkingHoursDto> workingHours
) {}
