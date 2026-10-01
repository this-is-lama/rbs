package my.project.restaurantservice.manager.consistency.outbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.kafka.KafkaProducer;
import my.project.restaurantservice.manager.consistency.outbox.entity.OutboxManagerEntity;
import my.project.restaurantservice.manager.consistency.outbox.repository.OutboxManagerRepository;
import my.project.restaurantservice.manager.event.ManagerRestaurantsChangedEvent;
import my.project.restaurantservice.manager.service.query.ManagerQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessorService {

    private final OutboxManagerRepository repository;
    private final ManagerQueryService managerQueryService;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public void process(UUID eventId) {
        OutboxManagerEntity entity = repository.findById(eventId).orElse(null);

        if (entity == null) {
            return;
        }

        UUID managerId = entity.getManagerId();
        boolean hasRestaurants = managerQueryService.hasAnyRestaurant(managerId);

        try {
            kafkaProducer.sendManagerRestaurantsChanged(new ManagerRestaurantsChangedEvent(managerId, hasRestaurants));
            repository.delete(entity);
        } catch (Exception e) {
            entity.setAttempts(entity.getAttempts() + 1);
            entity.setLastError(e.getClass().getSimpleName() + ": " + e.getMessage());
            log.error("Ошибка отправки OutboxEvent в Kafka, id={}, managerId={}, attempts={}",
                    entity.getId(), managerId, entity.getAttempts(), e);
        }
    }

}