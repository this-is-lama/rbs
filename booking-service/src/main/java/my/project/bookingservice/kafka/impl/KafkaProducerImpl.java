package my.project.bookingservice.kafka.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.events.BookingCancelledEvent;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import my.project.bookingservice.kafka.KafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerImpl implements KafkaProducer {

	private final KafkaTemplate<String, BookingCreatedEvent> bookingCreatedKafkaTemplate;
	private final KafkaTemplate<String, BookingCancelledEvent> bookingCancelledKafkaTemplate;

	@Value("${app.kafka.topics.booking-created}")
	private String bookingCreatedTopic;

	@Value("${app.kafka.topics.booking-cancelled}")
	private String bookingCancelledTopic;

	public void sendBookingCreated(BookingCreatedEvent event) {
		String key = event.bookingId().toString();

		log.info("Отправка события о создании бронирования в Kafka, bookingId={}, topic={}",
				event.bookingId(), bookingCreatedTopic);

		bookingCreatedKafkaTemplate.send(bookingCreatedTopic, key, event).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Не удалось отправить BookingCreatedEvent, key={}", key, ex);
			} else {
				log.info("BookingCreatedEvent успешно отправлен, key={}", key);
			}
		});
	}

	public void sendBookingCancelled(BookingCancelledEvent event) {
		String key = event.bookingId().toString();

		log.info("Отправка события об отмене бронирования в Kafka, bookingId={}, topic={}",
				event.bookingId(), bookingCancelledTopic);

		bookingCancelledKafkaTemplate.send(bookingCancelledTopic, key, event).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Не удалось отправить BookingCancelledEvent, key={}", key, ex);
			} else {
				log.info("BookingCancelledEvent успешно отправлен, key={}", key);
			}
		});
	}
}