package my.project.restaurantservice.dto.table;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record TableLayoutItemRequest(

        @NotNull
        UUID id,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double positionX,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double positionY,

        @NotNull
        @Positive
        Integer markerSize
) {}