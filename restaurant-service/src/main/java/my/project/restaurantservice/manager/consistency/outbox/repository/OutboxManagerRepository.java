package my.project.restaurantservice.manager.consistency.outbox.repository;

import jakarta.persistence.LockModeType;
import my.project.restaurantservice.manager.consistency.outbox.entity.OutboxManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxManagerRepository extends JpaRepository<OutboxManagerEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OutboxManagerEntity> findById(UUID id);

    List<OutboxManagerEntity> findTop100ByOrderByCreatedAtAsc();

    Optional<OutboxManagerEntity> findFirstByOrderByCreatedAtAsc();
}