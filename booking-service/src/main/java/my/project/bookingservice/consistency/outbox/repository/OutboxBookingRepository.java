package my.project.bookingservice.consistency.outbox.repository;

import jakarta.persistence.LockModeType;
import my.project.bookingservice.consistency.outbox.entity.OutboxBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxBookingRepository extends JpaRepository<OutboxBookingEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<OutboxBookingEntity> findById(UUID id);

	List<OutboxBookingEntity> findTop100ByOrderByCreatedAtAsc();

	Optional<OutboxBookingEntity> findFirstByOrderByCreatedAtAsc();
}
