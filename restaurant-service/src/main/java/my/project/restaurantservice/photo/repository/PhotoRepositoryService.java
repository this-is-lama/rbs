package my.project.restaurantservice.photo.repository;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.photo.entity.PhotoEntity;
import my.project.restaurantservice.photo.entity.enums.PhotoCategory;
import my.project.restaurantservice.photo.entity.enums.PhotoStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoRepositoryService {

	private final PhotoRepository repository;

	@Transactional(readOnly = true)
	public PhotoEntity getByIdAndObjectKeyAndStatus(UUID id, String objectKey, PhotoStatus status) {
		return repository.findByIdAndObjectKeyAndStatus(id, objectKey, status)
				.orElseThrow(() -> new NotFoundException("restaurant.photo.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> getAllByIdIn(Set<UUID> ids) {
		var photos = repository.findAllByIdIn(ids);
		if (photos.size() != ids.size()) {
			throw new NotFoundException("restaurant.photo.not-found", ids);
		}
		return photos;
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findAllByRestaurantIdAndStatus(UUID restId, PhotoStatus status) {
		return repository.findAllByRestaurantIdAndStatus(restId, status);
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findAllByDishIdAndStatus(UUID dishId, PhotoStatus status) {
		return repository.findAllByDishIdAndStatus(dishId, status);
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findAllByDishIdInAndStatusOrderBySortOrderAsc(Collection<UUID> dishIds, PhotoStatus status) {
		return repository.findAllByDishIdInAndStatusOrderBySortOrderAsc(dishIds, status);
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findFirstPhotosForRestaurants(Set<UUID> restIds, PhotoCategory category) {
		return repository.findFirstPhotosForRestaurants(restIds, category);
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findTop500ByStatus(PhotoStatus status) {
		return repository.findTop500ByStatus(status);
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findTop500ByStatusAndUploadedAtBefore(PhotoStatus status, Instant threshold) {
		return repository.findTop500ByStatusAndUploadedAtBefore(status, threshold);
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
