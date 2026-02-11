package my.project.notificationservice.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingCreatedEvent(

		UUID bookingId,

		String email,

		Instant startAt,
		Instant endAt,

		Integer guests,
		String comment,
		BigDecimal totalAmount,
		
		//restaurant
		String restaurantName,
		String restaurantDescription,
		String restaurantAddress,
		
		//table
		Integer tableNumber,
		String tableDescription

) {}