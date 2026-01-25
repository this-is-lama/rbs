package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {

	List<PhotoEntity> findTop500ByStatusAndUploadedAtBefore(PhotoStatus status, Instant threshold);

	List<PhotoEntity> findTop500ByStatus(PhotoStatus status);

	Optional<PhotoEntity> findByIdAndObjectKey(UUID id, String objectKey);
}
