package my.project.restaurantservice.dto;

import java.util.UUID;

public record DishDto(
		UUID id,
		String name,
		String category,
		String description,
		Integer price,
		Integer weight,
		boolean isAvailable
) {}
