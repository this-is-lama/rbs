package my.project.bookingservice.dto.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingCreatedEvent(

		UUID bookingId,

		String email,
		String username,

		Instant startAt,
		Instant endAt,

	Integer guests,
	String comment,
	BigDecimal totalAmount,
	BigDecimal preorderAmount,
	BigDecimal pricingCharge,

		//restaurant
		String restaurantName,
		String restaurantDescription,
		String restaurantAddress,

		//table
		Integer tableNumber,
		String tableDescription

) {}
