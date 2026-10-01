package my.project.bookingservice.config;

import lombok.extern.slf4j.Slf4j;
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
		return new NewTopic(bookingCreatedTopic, 3, (short) 1);
	}

	@Bean
	public NewTopic bookingCancelledTopic() {
		log.info("Создание Kafka topic bean для топика {}", bookingCancelledTopic);
		return new NewTopic(bookingCancelledTopic, 3, (short) 1);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		log.info("Инициализация универсального KafkaTemplate");
		KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
		template.setObservationEnabled(true);
		return template;
	}
}
