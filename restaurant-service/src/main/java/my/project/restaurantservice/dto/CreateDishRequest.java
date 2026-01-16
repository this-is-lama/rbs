package my.project.restaurantservice.dto;

public record CreateDishRequest(
		String name,
		String category,
		String description,
		Integer price,
		Integer weight,
		boolean isAvailable
) {}
