package my.project.restaurantservice.restaurant.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.restaurant.entity.ContactType;

public record ContactDto(

		@NotNull
		ContactType type,

		@NotBlank
		@Size(max = 255)
		String value
) {}
