package my.project.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

	@KafkaListener(topics = "booking-topic", groupId = "my-consumer")
	public void listen(ConsumerRecord<String, String> record) {
	}
}
