package my.project.restaurantservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record RestaurantDto(
		UUID id,

		@NotBlank
		@Size(max = 255)
		String name,

		@Size(max = 2000)
		String description,

		@NotBlank
		@Size(max = 100)
		String category,

		@Size(max = 30)
		String phone,

		@NotBlank
		@Size(max = 255)
		String address,

		Boolean isActive,

		List<@Valid WorkingHoursDto> workingHours,
		List<@Valid ContactDto> contacts,
		List<@Valid DishDto> dishes,
		List<@Valid TableDto> tables,
		List<PhotoDto> photos
) {}
