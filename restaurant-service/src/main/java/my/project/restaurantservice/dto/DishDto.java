package my.project.restaurantservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DishDto(
		UUID id,
		String name,
		String category,
		String description,
		BigDecimal price,
		Integer weight,
		boolean isAvailable
) {}
