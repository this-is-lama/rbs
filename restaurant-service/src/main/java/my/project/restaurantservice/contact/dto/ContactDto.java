package my.project.restaurantservice.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.contact.entity.ContactType;

public record ContactDto(

		@NotNull
		ContactType type,

		@NotBlank
		@Size(max = 255)
		String value
) {}
