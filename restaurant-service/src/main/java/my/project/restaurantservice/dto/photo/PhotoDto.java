package my.project.restaurantservice.dto.photo;

import my.project.restaurantservice.entity.enums.PhotoCategory;

import java.util.UUID;

public record PhotoDto(

		UUID id,

		String objectKey,

		String publicUrl,

		String contentType,

		PhotoCategory category,

		int sortOrder
) {}
