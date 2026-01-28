package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

	private final PhotoRepository repository;

	@Transactional(readOnly = true)
	public PhotoEntity findByIdAndObjectKey(UUID id, String objectKey) {
		return repository.findByIdAndObjectKey(id, objectKey)
				.orElseThrow(() -> new NotFoundException("restaurant.photo.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findTop500ByStatus(PhotoStatus status) {
		return repository.findTop500ByStatus(status);
	}

	@Transactional
	public List<PhotoEntity> saveAll(List<PhotoEntity> photos) {
		return repository.saveAll(photos);
	}

	@Transactional
	public void deleteAllById(List<UUID> ids) {
		repository.deleteAllById(ids);
	}

	@Transactional
	public void markExpired() {
		Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
		var photos = repository.findTop500ByStatusAndUploadedAtBefore(PhotoStatus.PENDING, threshold);
		photos.forEach(PhotoEntity::expired);
	}

	@Transactional
	public void markDeleting(List<UUID> ids) {
		List<PhotoEntity> photos = repository.findAllById(ids);
		if (photos.size() != ids.size()) {
			throw new NotFoundException("restaurant.photos.not-found", ids);
		}
		photos.forEach(PhotoEntity::deleting);
	}
}
