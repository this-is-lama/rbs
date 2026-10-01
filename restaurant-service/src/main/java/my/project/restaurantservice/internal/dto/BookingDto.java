package my.project.restaurantservice.internal.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingDto(
		UUID id,
		BookingStatus status,
		Instant endAt
) {}
