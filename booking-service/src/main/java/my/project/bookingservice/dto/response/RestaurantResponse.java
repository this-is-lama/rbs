package my.project.bookingservice.dto.response;

import my.project.bookingservice.entity.BookingEntity;

import java.util.UUID;

public record RestaurantResponse(

		UUID id,

		BookingEntity booking,

		UUID restaurantId,

		String name,

		String category,

		String description,

		String address

) {}
