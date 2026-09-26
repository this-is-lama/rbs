package my.project.notificationservice.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.config.KafkaConfig;
import my.project.notificationservice.events.BookingCancelledEvent;
import my.project.notificationservice.events.BookingCreatedEvent;
import my.project.notificationservice.service.NotificationService;
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

	/** В Prometheus превращается в notification_kafka_dead_letter_total — на неё смотрит алерт rbs-kafka-dead-letter. */
	private static final String DEAD_LETTER_METRIC = "notification.kafka.dead.letter";

	private final NotificationService notificationService;
	private final MeterRegistry meterRegistry;

	@Value("${app.kafka.topics.booking-created}")
	private String bookingCreatedTopic;

	@Value("${app.kafka.topics.booking-cancelled}")
	private String bookingCancelledTopic;

	/**
	 * Счётчики создаются заранее со значением 0. Если счётчик появится только в момент первой ошибки
	 * (сразу со значением 1), increase() в Prometheus не увидит прироста и алерт промолчит.
	 */
	@PostConstruct
	void registerDeadLetterCounters() {
		deadLetterCounter(bookingCreatedTopic);
		deadLetterCounter(bookingCancelledTopic);
	}

	@RetryableTopic(
			attempts = "4",
			backOff = @BackOff(delay = 1000, multiplier = 10.0, maxDelay = 120000),
			exclude = {
					NullPointerException.class
			},
			numPartitions = "3",
			kafkaTemplate = KafkaConfig.RETRY_KAFKA_TEMPLATE,
			dltTopicSuffix = KafkaConfig.DLT_SUFFIX
	)
	@KafkaListener(
			topics = "${app.kafka.topics.booking-created}",
			groupId = "${spring.kafka.consumer.group-id}",
			properties = "spring.json.value.default.type=my.project.notificationservice.events.BookingCreatedEvent"
	)
	public void listenBookingCreated(ConsumerRecord<String, BookingCreatedEvent> consumerRecord) {
		var event = consumerRecord.value();
		var key = consumerRecord.key();

		log.info("Получено событие создания бронирования из Kafka, key={}, topic={}, partition={}, offset={}",
				key,
				consumerRecord.topic(),
				consumerRecord.partition(),
				consumerRecord.offset());

		notificationService.send(event);
	}

	@RetryableTopic(
			attempts = "4",
			backOff = @BackOff(delay = 1000, multiplier = 10.0, maxDelay = 120000),
			exclude = {
					NullPointerException.class
			},
			numPartitions = "3",
			kafkaTemplate = KafkaConfig.RETRY_KAFKA_TEMPLATE,
			dltTopicSuffix = KafkaConfig.DLT_SUFFIX
	)
	@KafkaListener(
			topics = "${app.kafka.topics.booking-cancelled}",
			groupId = "${spring.kafka.consumer.group-id}",
			properties = "spring.json.value.default.type=my.project.notificationservice.events.BookingCancelledEvent"
	)
	public void listenBookingCancelled(ConsumerRecord<String, BookingCancelledEvent> consumerRecord) {
		var event = consumerRecord.value();
		var key = consumerRecord.key();

		log.info("Получено событие отмены бронирования из Kafka, key={}, topic={}, partition={}, offset={}",
				key,
				consumerRecord.topic(),
				consumerRecord.partition(),
				consumerRecord.offset());

		notificationService.send(event);
	}


	@DltHandler
	public void handleDlt(ConsumerRecord<String, Object> record) {
		// booking-created-topic-dlt -> booking-created-topic. Берём из имени DLT, а не из заголовка:
		// после нескольких retry-топиков в заголовке original-topic может оказаться ...-retry-N
		var topic = record.topic().substring(0, record.topic().length() - KafkaConfig.DLT_SUFFIX.length());
		var message = getHeader(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE);

		deadLetterCounter(topic).increment();
		log.error("Сообщение ушло в DLT: key={}, topic={}, message={}", record.key(), topic, message);
	}

	private Counter deadLetterCounter(String topic) {
		// register() возвращает уже существующий счётчик, если он зарегистрирован с тем же тегом
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