package my.project.userservice.repository;

import my.project.userservice.entity.RefreshJtiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefreshJtiRepository extends JpaRepository<RefreshJtiEntity, UUID> {

	long deleteAllByActiveFalseOrExpiresAtBefore(Instant now);

	List<RefreshJtiEntity> findAllByUserIdAndActiveTrue(UUID userId);
}
