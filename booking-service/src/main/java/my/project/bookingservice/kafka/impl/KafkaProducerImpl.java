package my.project.bookingservice.kafka.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.events.BookingCancelledEvent;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import my.project.bookingservice.kafka.KafkaProducer;
import my.project.common.logging.Loggable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerImpl implements KafkaProducer {

	private static final long SEND_TIMEOUT_SECONDS = 5;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${app.kafka.topics.booking-created}")
	private String bookingCreatedTopic;

	@Value("${app.kafka.topics.booking-cancelled}")
	private String bookingCancelledTopic;

	public void sendBookingCreated(BookingCreatedEvent event) {
		send(bookingCreatedTopic, event.bookingId().toString(), event);
	}

	public void sendBookingCancelled(BookingCancelledEvent event) {
		send(bookingCancelledTopic, event.bookingId().toString(), event);
	}

	private void send(String topic, String key, Object event) {
		String eventName = event.getClass().getSimpleName();

		try {
			kafkaTemplate.send(topic, key, event).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			log.info("{} успешно отправлен, key={}", eventName, key);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Отправка " + eventName + " прервана, key=" + key, e);
		} catch (ExecutionException | TimeoutException e) {
			throw new IllegalStateException("Не удалось отправить " + eventName + ", key=" + key, e);
		}
	}
}
