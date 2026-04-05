package my.project.bookingservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;

	@Value("${app.kafka.topics.booking-created}")
	private String bookingCreatedTopic;

	public void sendBookingCreated(BookingCreatedEvent event) {
		String key = event.bookingId().toString();

		log.info("Отправка события о создании бронирования в Kafka, bookingId={}, topic={}",
				event.bookingId(), bookingCreatedTopic);

		kafkaTemplate.send(bookingCreatedTopic, key, event).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Не удалось отправить BookingCreatedEvent, key={}", key, ex);
			} else {
				log.info("BookingCreatedEvent успешно отправлен, key={}", key);
			}
		});
	}
}