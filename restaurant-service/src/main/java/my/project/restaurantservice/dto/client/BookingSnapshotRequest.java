package my.project.restaurantservice.dto.client;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record BookingSnapshotRequest(

		@NotNull
		UUID tableId,

		Set<UUID> dishes
) {}
