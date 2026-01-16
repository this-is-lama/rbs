package my.project.restaurantservice.dto;

import my.project.restaurantservice.entity.ContactType;

public record ContactDto(
		ContactType type,
		String value
) {}
