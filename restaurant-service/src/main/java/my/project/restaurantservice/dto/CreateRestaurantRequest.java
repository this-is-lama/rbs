package my.project.restaurantservice.dto;

import java.util.List;

public record CreateRestaurantRequest(
		String name,
		String description,
		String category,
		String phone,
		String address,
		Boolean isActive,
		List<WorkingHoursDto> workingHours,
		List<ContactDto> contacts
) {}

