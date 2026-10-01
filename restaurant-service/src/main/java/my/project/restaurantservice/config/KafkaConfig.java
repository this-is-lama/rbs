package my.project.restaurantservice.config;

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

	@Value("${app.kafka.topics.manager-restaurants-changed}")
	private String managerRestaurantsChangedTopic;

	@Bean
	public NewTopic managerRestaurantsChangedTopic() {
		log.info("Создание Kafka topic bean для топика {}", managerRestaurantsChangedTopic);
		return new NewTopic(managerRestaurantsChangedTopic, 3, (short) 1);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		log.info("Инициализация универсального KafkaTemplate");
		KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
		template.setObservationEnabled(true);
		return template;
	}
}
