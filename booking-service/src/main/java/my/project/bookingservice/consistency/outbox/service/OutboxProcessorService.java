package my.project.bookingservice.consistency.outbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.consistency.outbox.entity.OutboxBookingEntity;
import my.project.bookingservice.consistency.outbox.repository.OutboxBookingRepository;
import my.project.bookingservice.dto.events.BookingCancelledEvent;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import my.project.bookingservice.kafka.KafkaProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessorService {

	private static final int MAX_ERROR_LENGTH = 1000;

	private final OutboxBookingRepository repository;
	private final KafkaProducer kafkaProducer;
	private final JsonMapper jsonMapper;

	@Transactional
	public void process(UUID eventId) {
		OutboxBookingEntity entity = repository.findById(eventId).orElse(null);
		if (entity == null) {
			return;
		}

		try {
			send(entity);
			repository.delete(entity);
		} catch (Exception e) {
			entity.setAttempts(entity.getAttempts() + 1);
			entity.setLastError(cut(e.getClass().getSimpleName() + ": " + e.getMessage()));
			log.error("Ошибка отправки OutboxEvent в Kafka, id={}, type={}, bookingId={}, attempts={}",
					entity.getId(), entity.getEventType(), entity.getBookingId(), entity.getAttempts(), e);
		}
	}

	private void send(OutboxBookingEntity entity) {
		switch (entity.getEventType()) {
			case BOOKING_CREATED -> kafkaProducer.sendBookingCreated(
					jsonMapper.readValue(entity.getPayload(), BookingCreatedEvent.class));
			case BOOKING_CANCELLED -> kafkaProducer.sendBookingCancelled(
					jsonMapper.readValue(entity.getPayload(), BookingCancelledEvent.class));
		}
	}

	private String cut(String message) {
		return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
	}
}
