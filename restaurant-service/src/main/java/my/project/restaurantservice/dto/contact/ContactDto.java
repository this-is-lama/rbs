package my.project.restaurantservice.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.entity.enums.ContactType;

public record ContactDto(

		@NotNull
		ContactType type,

		@NotBlank
		@Size(max = 255)
		String value
) {}
