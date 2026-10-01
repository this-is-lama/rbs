package my.project.restaurantservice.kafka.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.kafka.KafkaProducer;
import my.project.restaurantservice.manager.event.ManagerRestaurantsChangedEvent;
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

	@Value("${app.kafka.topics.manager-restaurants-changed}")
	private String managerRestaurantsChangedTopic;

	public void sendManagerRestaurantsChanged(ManagerRestaurantsChangedEvent event) {
		String key = event.managerId().toString();

		try {
			kafkaTemplate.send(managerRestaurantsChangedTopic, key, event)
					.get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			log.info("ManagerRestaurantsChangedEvent успешно отправлен, key={}, hasRestaurants={}", key, event.hasRestaurants());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Отправка ManagerRestaurantsChangedEvent прервана, key=" + key, e);
		} catch (ExecutionException | TimeoutException e) {
			throw new IllegalStateException("Не удалось отправить ManagerRestaurantsChangedEvent, key=" + key, e);
		}
	}
}
