package my.project.bookingservice.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record DishResponse(

        UUID id,

        UUID dishId,

        String name,

        BigDecimal price,

        Integer quantity

) {}
