package my.project.restaurantservice.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.contact.dto.ContactDto;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;

import java.util.List;

public record RestaurantCreateRequest(

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
		String house,

		@NotEmpty
		List<@Valid WorkingHoursDto> workingHours,

		@NotEmpty
		List<@Valid ContactDto> contacts
) {}
