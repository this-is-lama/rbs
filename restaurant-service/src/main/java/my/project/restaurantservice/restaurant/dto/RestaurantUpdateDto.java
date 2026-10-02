package my.project.restaurantservice.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestaurantUpdateDto(

		@NotBlank
		@Size(max = 255)
		String name,

		@NotBlank
		@Size(max = 100)
		String category,

		@Size(max = 2000)
		String description,

		@NotBlank
		@Size(max = 100)
		String city,

		@NotBlank
		@Size(max = 255)
		String street,

		@NotBlank
		@Size(max = 20)
		String house
) {}
