package my.project.bookingservice.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingDishResponse(

        UUID id,

        UUID dishId,

        String dishName,

        BigDecimal price,

        Integer quantity

) {}
