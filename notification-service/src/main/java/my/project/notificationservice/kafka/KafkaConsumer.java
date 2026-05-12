package my.project.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.events.BookingCancelledEvent;
import my.project.notificationservice.events.BookingCreatedEvent;
import my.project.notificationservice.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

	private final NotificationService notificationService;

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

		log.info("Событие о создании бронирования принято в обработку, key={}", key);
	}

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

		log.info("Событие об отмене бронирования принято в обработку, key={}", key);
	}
}