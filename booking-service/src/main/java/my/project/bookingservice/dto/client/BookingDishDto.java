package my.project.bookingservice.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingDishDto(

		UUID id,

		@NotBlank
		@Size(max = 255)
		String name,

		@NotNull
		@Positive
		BigDecimal price

) {}
