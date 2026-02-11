package my.project.bookingservice.config;

import my.project.bookingservice.dto.events.BookingCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

	@Value("${app.kafka.topics.booking-created}")
	private String bookingTopic;

	@Bean
	public NewTopic bookingTopic() {
		return new NewTopic(bookingTopic, 1, (short) 1);
	}

	@Bean
	public KafkaTemplate<String, BookingCreatedEvent> bookingEventKafkaTemplate(
			ProducerFactory<String, BookingCreatedEvent> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
}
