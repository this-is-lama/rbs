package my.project.restaurantservice.dto;

import java.util.UUID;

public record TableDto(
		UUID id,
		int tableNumber,
		String description,
		int capacity,
		boolean isActive
) {}
