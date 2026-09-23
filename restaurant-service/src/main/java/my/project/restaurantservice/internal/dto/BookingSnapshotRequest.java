package my.project.restaurantservice.internal.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record BookingSnapshotRequest(

		@NotNull
		UUID tableId,

		Set<UUID> dishes
) {}
