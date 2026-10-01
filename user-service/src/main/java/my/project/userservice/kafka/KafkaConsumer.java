package my.project.userservice.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.userservice.config.KafkaConfig;
import my.project.userservice.events.ManagerRestaurantsChangedEvent;
import my.project.userservice.service.user.UserCommandService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

	private static final String DEAD_LETTER_METRIC = "user.kafka.dead.letter";

	private final UserCommandService userCommandService;
	private final MeterRegistry meterRegistry;

	@Value("${app.kafka.topics.manager-restaurants-changed}")
	private String managerRestaurantsChangedTopic;

	@PostConstruct
	void registerDeadLetterCounters() {
		deadLetterCounter(managerRestaurantsChangedTopic);
	}

	@RetryableTopic(
			attempts = "4",
			backOff = @BackOff(delay = 1000, multiplier = 10.0, maxDelay = 120000),
			exclude = {
					NotFoundException.class,
					NullPointerException.class
			},
			numPartitions = "3",
			kafkaTemplate = KafkaConfig.RETRY_KAFKA_TEMPLATE,
			dltTopicSuffix = KafkaConfig.DLT_SUFFIX
	)
	@KafkaListener(
			topics = "${app.kafka.topics.manager-restaurants-changed}",
			groupId = "${spring.kafka.consumer.group-id}",
			properties = "spring.json.value.default.type=my.project.userservice.events.ManagerRestaurantsChangedEvent"
	)
	public void listenManagerRestaurantsChanged(ConsumerRecord<String, ManagerRestaurantsChangedEvent> consumerRecord) {
		var event = consumerRecord.value();
		var key = consumerRecord.key();

		log.info("Получено событие изменения ресторанов менеджера из Kafka, key={}, hasRestaurants={}, topic={}, partition={}, offset={}",
				key,
				event.hasRestaurants(),
				consumerRecord.topic(),
				consumerRecord.partition(),
				consumerRecord.offset());

		userCommandService.syncManagerRole(event.managerId(), event.hasRestaurants());
	}

	@DltHandler
	public void handleDlt(ConsumerRecord<String, Object> record) {
		var topic = record.topic().substring(0, record.topic().length() - KafkaConfig.DLT_SUFFIX.length());
		var message = getHeader(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE);

		deadLetterCounter(topic).increment();
		log.error("Сообщение ушло в DLT: key={}, topic={}, message={}", record.key(), topic, message);
	}

	private Counter deadLetterCounter(String topic) {
		return Counter.builder(DEAD_LETTER_METRIC)
				.description("Сообщения, ушедшие в dead-letter topic после всех повторов")
				.tag("topic", topic)
				.register(meterRegistry);
	}

	private String getHeader(ConsumerRecord<String, Object> record, String headerKey) {
		var header = record.headers().lastHeader(headerKey);
		return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
	}
}
