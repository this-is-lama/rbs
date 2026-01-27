package my.project.restaurantservice.dto.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.dto.workinghours.WorkingHoursDto;
import my.project.restaurantservice.dto.contact.ContactDto;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.dto.photo.PhotoResponse;
import my.project.restaurantservice.dto.table.TableDto;

import java.util.List;
import java.util.UUID;

public record RestaurantDto(

		UUID id,

		@NotBlank
		@Size(max = 255)
		String name,

		@Size(max = 2000)
		String description,

		@NotBlank
		@Size(max = 100)
		String category,

		@NotBlank
		@Size(max = 255)
		String address,

		Boolean active,

		List<@Valid WorkingHoursDto> workingHours,
		List<@Valid ContactDto> contacts,
		List<@Valid DishDto> dishes,
		List<@Valid TableDto> tables,
		List<PhotoResponse> photos
) {}
