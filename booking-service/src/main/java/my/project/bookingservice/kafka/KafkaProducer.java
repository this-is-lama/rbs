package my.project.bookingservice.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;

	@Value("${app.kafka.topics.booking-created}")
	private String bookingCreatedTopic;

	public void sendBookingCreated(BookingCreatedEvent event) {
		String key = event.bookingId().toString();
		kafkaTemplate.send(bookingCreatedTopic, key, event).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Failed to send BookingCreatedEvent. key={}", key, ex);
			} else {
				log.info("BookingCreatedEvent sent. key={}", key);
			}
		});
	}
}
