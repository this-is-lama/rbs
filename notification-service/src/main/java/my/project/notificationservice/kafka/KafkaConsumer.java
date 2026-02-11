package my.project.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void listen(ConsumerRecord<String, BookingCreatedEvent> consumerRecord) {
		var event = consumerRecord.value();
		var key = consumerRecord.key();
		notificationService.send(event);
		log.info("Booking created event accepted: key={}", key);
	}

}
