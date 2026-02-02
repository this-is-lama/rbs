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
	public PhotoEntity findPending(UUID id, String objectKey) {
		return repository.findByIdAndObjectKeyAndStatus(id, objectKey, PhotoStatus.PENDING)
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
	public List<PhotoEntity> findAllById(List<UUID> ids) {
		var photos = repository.findAllByIdIn(ids);
		if (photos.size() != ids.size()) {
			throw new NotFoundException("restaurant.photo.not-found", ids);
		}
		return photos;
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
	public void markDeleting(List<PhotoEntity> photos) {
		photos.forEach(PhotoEntity::deleting);
	}
}
