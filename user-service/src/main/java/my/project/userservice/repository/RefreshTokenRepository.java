package my.project.userservice.repository;

import my.project.userservice.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

	long deleteAllByActiveFalseOrExpiresAtBefore(Instant now);

	List<RefreshTokenEntity> findAllByUserIdAndActiveTrue(UUID userId);
}
