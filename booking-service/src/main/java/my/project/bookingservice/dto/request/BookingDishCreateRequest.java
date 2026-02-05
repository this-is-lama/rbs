package my.project.bookingservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookingDishCreateRequest(

        @NotNull
        UUID dishId,

        @NotNull
        @Min(1)
        @Max(100)
        Integer quantity

) {}
