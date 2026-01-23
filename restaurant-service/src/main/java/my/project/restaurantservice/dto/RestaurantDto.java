package my.project.restaurantservice.dto;

import java.util.List;
import java.util.UUID;

public record RestaurantDto(
		UUID id,
		String name,
		String description,
		String category,
		String phone,
		String address,
		Boolean isActive,
		List<WorkingHoursDto> workingHours,
		List<ContactDto> contacts,
		List<DishDto> dishes,
		List<TableDto> tables,
		List<PhotoDto> photos
) {}
