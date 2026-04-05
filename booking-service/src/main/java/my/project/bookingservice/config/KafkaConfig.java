package my.project.bookingservice.config;

import lombok.extern.slf4j.Slf4j;
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
	private String bookingTopic;

	@Bean
	public NewTopic bookingTopic() {
		log.info("Создание Kafka topic bean для топика {}", bookingTopic);
		return new NewTopic(bookingTopic, 1, (short) 1);
	}

	@Bean
	public KafkaTemplate<String, BookingCreatedEvent> bookingEventKafkaTemplate(
			ProducerFactory<String, BookingCreatedEvent> producerFactory) {
		log.info("Инициализация KafkaTemplate для событий бронирования");
		return new KafkaTemplate<>(producerFactory);
	}
}