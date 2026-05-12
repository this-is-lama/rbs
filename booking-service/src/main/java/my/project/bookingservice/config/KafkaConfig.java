package my.project.bookingservice.config;

import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.events.BookingCancelledEvent;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Slf4j
@Configuration
public class KafkaConfig {

	@Value("${app.kafka.topics.booking-created}")
	private String bookingCreatedTopic;

	@Value("${app.kafka.topics.booking-cancelled}")
	private String bookingCancelledTopic;

	@Bean
	public NewTopic bookingCreatedTopic() {
		log.info("Создание Kafka topic bean для топика {}", bookingCreatedTopic);
		return new NewTopic(bookingCreatedTopic, 1, (short) 1);
	}

	@Bean
	public NewTopic bookingCancelledTopic() {
		log.info("Создание Kafka topic bean для топика {}", bookingCancelledTopic);
		return new NewTopic(bookingCancelledTopic, 1, (short) 1);
	}

	@Bean
	public KafkaTemplate<String, BookingCreatedEvent> bookingCreatedEventKafkaTemplate(
			ProducerFactory<String, BookingCreatedEvent> producerFactory) {
		log.info("Инициализация KafkaTemplate для событий создания бронирования");
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	public KafkaTemplate<String, BookingCancelledEvent> bookingCancelledEventKafkaTemplate(
			ProducerFactory<String, BookingCancelledEvent> producerFactory) {
		log.info("Инициализация KafkaTemplate для событий отмены бронирования");
		return new KafkaTemplate<>(producerFactory);
	}
}