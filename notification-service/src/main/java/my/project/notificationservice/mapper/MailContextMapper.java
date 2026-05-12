package my.project.notificationservice.mapper;

import my.project.notificationservice.events.BookingCancelledEvent;
import my.project.notificationservice.events.BookingNotificationEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MailContextMapper {

	public Map<String, Object> toContext(BookingNotificationEvent event) {
		Map<String, Object> context = new HashMap<>();

		context.put("bookingId", event.bookingId());
		context.put("username", event.username());

		context.put("startAt", event.startAt());
		context.put("endAt", event.endAt());

		context.put("guests", event.guests());
		context.put("comment", event.comment());
		context.put("totalAmount", event.totalAmount());

		// restaurant
		context.put("restaurantName", event.restaurantName());
		context.put("restaurantDescription", event.restaurantDescription());
		context.put("restaurantAddress", event.restaurantAddress());

		// table
		context.put("tableNumber", event.tableNumber());
		context.put("tableDescription", event.tableDescription());

		if (event instanceof BookingCancelledEvent cancelledEvent) {
			context.put("reason", cancelledEvent.reason());
		}

		return context;
	}
}