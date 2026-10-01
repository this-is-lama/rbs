package my.project.bookingservice.consistency.outbox.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.consistency.outbox.entity.OutboxBookingEntity;
import my.project.bookingservice.consistency.outbox.repository.OutboxBookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxWorker {

	private final OutboxBookingRepository outboxRepository;
	private final OutboxProcessorService outboxProcessorService;

	@Scheduled(fixedDelay = 5000)
	public void process() {
		List<OutboxBookingEntity> events = outboxRepository.findTop100ByOrderByCreatedAtAsc();
		for (OutboxBookingEntity event : events) {
			outboxProcessorService.process(event.getId());
		}
	}
}
