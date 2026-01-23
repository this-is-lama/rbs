package my.project.restaurantservice.dto;

import my.project.restaurantservice.entity.PhotoCategory;

public record PhotoDto(
		String objectKey,
        PhotoCategory category,
        int sortOrder
) {}
