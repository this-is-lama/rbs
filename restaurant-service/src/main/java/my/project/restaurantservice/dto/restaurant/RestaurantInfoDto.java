package my.project.restaurantservice.dto.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.dto.workinghours.WorkingHoursDto;

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

		Boolean active,

		List<@Valid WorkingHoursDto> workingHours
) {}

