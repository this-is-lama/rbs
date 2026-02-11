package my.project.bookingservice.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookingRestaurantDto(

		UUID id,

		@NotBlank
		@Size(max = 255)
		String name,

		@NotBlank
		@Size(max = 100)
		String category,

		@Size(max = 2000)
		String description,

		@NotBlank
		@Size(max = 255)
		String address
) {}
