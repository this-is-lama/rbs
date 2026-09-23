package my.project.restaurantservice.dish.dto;

import my.project.restaurantservice.photo.dto.PhotoDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DishDetailsDto(

		UUID id,

		String name,

		String category,

		String description,

		BigDecimal price,

		Integer weight,

		Boolean available,

		List<PhotoDto> photos
) {}
