package my.project.bookingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

	@Bean
	public NewTopic bookingTopic() {
		return new NewTopic("booking-topic", 1, (short) 1);
	}
}
