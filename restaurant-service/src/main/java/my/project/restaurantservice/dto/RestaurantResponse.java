package my.project.restaurantservice.dto;

import my.project.restaurantservice.entity.DishEntity;
import my.project.restaurantservice.entity.TableEntity;

import java.util.List;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
		String name,
		String description,
		String category,
		String phone,
		String address,
		Boolean isActive,
		List<WorkingHoursDto> workingHours,
		List<ContactDto> contacts,
		List<DishEntity> dishes,
		List<TableEntity> tables
) {}
