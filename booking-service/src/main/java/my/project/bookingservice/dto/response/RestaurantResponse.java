package my.project.bookingservice.dto.response;

import java.util.UUID;

public record RestaurantResponse(

		UUID id,

		UUID restaurantId,

		String name,

		String category,

		String description,

		String address

) {}
