package my.project.notificationservice.events;

import my.project.notificationservice.entity.MessageType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingCancelledEvent(

		UUID bookingId,

		String email,
		String username,

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
		String tableDescription,

		String reason

) implements BookingNotificationEvent {

	@Override
	public MessageType messageType() {
		return MessageType.BOOKING_CANCELLED;
	}
}