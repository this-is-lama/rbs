package my.project.restaurantservice.manager.consistency.outbox.service;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.manager.consistency.outbox.entity.OutboxManagerEntity;
import my.project.restaurantservice.manager.consistency.outbox.repository.OutboxManagerRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxWorker {

    private final OutboxManagerRepository outboxRepository;
    private final OutboxProcessorService outboxProcessorService;

    @Scheduled(fixedDelay = 5000)
    public void process() {
        List<OutboxManagerEntity> events = outboxRepository.findTop100ByOrderByCreatedAtAsc();
        for (OutboxManagerEntity event : events) {
            outboxProcessorService.process(event.getId());
        }
    }
}