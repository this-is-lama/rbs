package my.project.bookingservice.dto.response;

import my.project.bookingservice.entity.BookingStatus;

import java.math.BigDecimal;
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

		BigDecimal totalAmount,
		BigDecimal preorderAmount,
		BigDecimal pricingCharge,
		UUID pricingOfferId,

		Instant createdAt,
		Instant cancelledAt,
		String cancellationReason,

		RestaurantResponse restaurant,
		TableResponse table,
		List<DishResponse> dishes

) {}
