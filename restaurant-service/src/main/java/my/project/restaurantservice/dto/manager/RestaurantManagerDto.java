package my.project.restaurantservice.dto.manager;

import java.time.Instant;
import java.util.UUID;

public record RestaurantManagerDto(
        UUID id,
        String name,
        String surname,
        String email,
        String role,
        boolean enabled,
        Instant assignedAt
) {}