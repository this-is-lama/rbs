package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	List<PhotoEntity> findTop500ByStatusAndUploadedAtBefore(PhotoStatus status, Instant threshold);

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	List<PhotoEntity> findTop500ByStatus(PhotoStatus status);

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	List<PhotoEntity> findAllByIdIn(List<UUID> ids);

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	Optional<PhotoEntity> findByIdAndObjectKeyAndStatus(UUID id, String objectKey, PhotoStatus status);

}
