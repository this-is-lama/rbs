package my.project.restaurantservice.dto;

import my.project.restaurantservice.entity.PhotoCategory;

public record PhotoMetaDto(
		PhotoCategory category,
		int sortOrder
) {}
