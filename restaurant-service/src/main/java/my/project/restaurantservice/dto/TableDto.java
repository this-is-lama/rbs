package my.project.restaurantservice.dto;

public record TableCreateRequest(
		int tableNumber,
		String description,
		int capacity,
		boolean isActive
) {}
