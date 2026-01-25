package my.project.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.entity.enums.ContactType;
import org.jetbrains.annotations.NotNull;

public record ContactDto(
		@NotNull
		ContactType type,

		@NotBlank
		@Size(max = 255)
		String value
) {}
