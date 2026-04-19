package my.project.restaurantservice.dto.restaurant;

import jakarta.validation.constraints.NotNull;

public record RestaurantActiveUpdateRequest(
        @NotNull
        Boolean active
) {}