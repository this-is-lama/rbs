package my.project.restaurantservice.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TableDto(
		UUID id,

		@Positive
		int tableNumber,

		@Size(max = 500)
		String description,

		@Positive
		int capacity,

		boolean isActive
) {}
