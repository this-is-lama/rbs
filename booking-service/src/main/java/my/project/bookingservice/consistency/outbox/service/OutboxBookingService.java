package my.project.bookingservice.consistency.outbox.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.consistency.outbox.entity.OutboxBookingEntity;
import my.project.bookingservice.consistency.outbox.entity.OutboxEventType;
import my.project.bookingservice.consistency.outbox.repository.OutboxBookingRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxBookingService {

	private final OutboxBookingRepository repository;
	private final JsonMapper jsonMapper;

	public UUID save(OutboxEventType eventType, UUID bookingId, Object event) {
		OutboxBookingEntity entity = OutboxBookingEntity.builder()
				.eventType(eventType)
				.bookingId(bookingId)
				.payload(jsonMapper.writeValueAsString(event))
				.build();

		return repository.save(entity).getId();
	}
}
