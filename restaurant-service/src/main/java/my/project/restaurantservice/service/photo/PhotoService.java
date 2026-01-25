package my.project.restaurantservice.service.photo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

	private final PhotoRepository repository;

	@Transactional(readOnly = true)
	public PhotoEntity findByIdAndObjectKey(UUID id, String objectKey) {
		return repository.findByIdAndObjectKey(id, objectKey)
				.orElseThrow(() -> new EntityNotFoundException(id.toString()));
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findTop500ByStatusAndUploadedAtBefore(PhotoStatus status, Instant threshold) {
		return repository.findTop500ByStatusAndUploadedAtBefore(status, threshold);
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
}
