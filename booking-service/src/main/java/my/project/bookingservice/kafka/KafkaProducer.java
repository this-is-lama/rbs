package my.project.bookingservice.kafka;

import my.project.bookingservice.dto.events.BookingCancelledEvent;
import my.project.bookingservice.dto.events.BookingCreatedEvent;

public interface KafkaProducer {

	void sendBookingCreated(BookingCreatedEvent event);

	void sendBookingCancelled(BookingCancelledEvent event);
}
