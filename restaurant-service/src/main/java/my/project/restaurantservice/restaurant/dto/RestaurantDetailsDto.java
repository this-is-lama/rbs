package my.project.restaurantservice.restaurant.dto;

import my.project.restaurantservice.dish.dto.DishDetailsDto;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.restaurant.dto.contact.ContactDto;
import my.project.restaurantservice.restaurant.dto.workinghours.WorkingHoursDto;
import my.project.restaurantservice.table.dto.TableDto;

import java.util.List;
import java.util.UUID;

public record RestaurantDetailsDto(

		UUID id,

		String name,

		String category,

		String description,

		String address,

		Boolean active,

		List<WorkingHoursDto> workingHours,

		List<ContactDto> contacts,

		List<DishDetailsDto> dishes,

		List<TableDto> tables,

		List<PhotoDto> photos
) {}
