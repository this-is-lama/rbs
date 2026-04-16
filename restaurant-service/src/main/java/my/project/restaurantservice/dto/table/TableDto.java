package my.project.restaurantservice.dto.table;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TableDto(

        UUID id,

        @NotNull
        @Positive
        Integer tableNumber,

        @Size(max = 500)
        String description,

        @NotNull
        @Positive
        Integer capacity,

        @NotNull
        Boolean active,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double positionX,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double positionY,

        @Positive
        Integer markerSize
) {}
