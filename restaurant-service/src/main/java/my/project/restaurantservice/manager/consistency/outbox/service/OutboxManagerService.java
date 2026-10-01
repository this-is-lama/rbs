package my.project.restaurantservice.manager.consistency.outbox.service;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.manager.consistency.outbox.entity.OutboxManagerEntity;
import my.project.restaurantservice.manager.consistency.outbox.repository.OutboxManagerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxManagerService {

    private final OutboxManagerRepository repository;

    public UUID save(UUID managerId) {
        OutboxManagerEntity entity = OutboxManagerEntity.builder()
                .managerId(managerId)
                .build();

        return repository.save(entity).getId();
    }

}