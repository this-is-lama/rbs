package my.project.restaurantservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record RestaurantInfoDto(
		UUID id,

		@NotBlank
		@Size(max = 255)
		String name,

		@NotBlank
		@Size(max = 100)
		String category,

		@NotBlank
		@Size(max = 255)
		String address,

		Boolean isActive,

		List<@Valid WorkingHoursDto> workingHours
) {}

