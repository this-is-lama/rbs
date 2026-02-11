package my.project.notificationservice.config;

import my.project.notificationservice.events.BookingCreatedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingCreatedEvent> kafkaListenerContainerFactory(ConsumerFactory<String, BookingCreatedEvent> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, BookingCreatedEvent>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
