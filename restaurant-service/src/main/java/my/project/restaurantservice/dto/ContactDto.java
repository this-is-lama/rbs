package my.project.restaurantservice.dto;

import my.project.restaurantservice.entity.enums.ContactType;

public record ContactDto(
		ContactType type,
		String value
) {}
