package my.project.bookingservice.dto.response;

import my.project.bookingservice.entity.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(

        UUID id,
        UUID restaurantId,
        UUID userId,

        Instant startAt,
        Instant endAt,

        BookingStatus status,

        Integer guests,

        String comment,

        Instant createdAt,
        Instant cancelledAt,

		RestaurantResponse restaurant,
		TableResponse table,
        List<DishResponse> dishes

) {}
