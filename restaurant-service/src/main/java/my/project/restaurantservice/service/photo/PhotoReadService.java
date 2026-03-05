package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.photo.PhotoDto;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.enums.PhotoCategory;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.mapper.PhotoMapper;
import my.project.restaurantservice.repository.PhotoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoReadService {

	private final PhotoRepository repository;
	private final PhotoMapper mapper;


	@Cacheable(
			cacheNames = "photosByRestaurantId",
			key = "#restId",
			sync = true
	)
	@Transactional(readOnly = true)
	public List<PhotoDto> getAllByRestaurantId(UUID restId) {
		var photos = repository.findAllByRestaurantIdAndStatus(restId, PhotoStatus.ACTIVE);
		return mapper.toDto(photos);
	}
	@CacheEvict(cacheNames = "photosByRestaurantId", key = "#restId")
	public void evictPhotosByRestaurantId(UUID restId) {}



	@Cacheable(
			cacheNames = "photosByDishId",
			key = "#dishId",
			sync = true
	)
	@Transactional(readOnly = true)
	public List<PhotoDto> getAllByDishId(UUID dishId) {
		var photos = repository.findAllByDishIdAndStatus(dishId, PhotoStatus.ACTIVE);
		return mapper.toDto(photos);
	}
	@CacheEvict(cacheNames = "photosByDishId", key = "#dishId")
	public void evictPhotosByDishId(UUID dishId) {}


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
	public List<PhotoEntity> findAllByIdIn(Set<UUID> ids) {
		var photos = repository.findAllByIdIn(ids);
		if (photos.size() != ids.size()) {
			throw new NotFoundException("restaurant.photo.not-found", ids);
		}
		return photos;
	}

	@Transactional(readOnly = true)
	public Map<UUID, PhotoEntity> findBannersForRestaurants(Set<UUID> restIds) {
		var photos = repository.findFirstPhotosForRestaurants(restIds, PhotoCategory.BANNER);
		return photos.stream().collect(Collectors.toMap(p -> p.getRestaurant().getId(), p -> p));
	}

	@Transactional(readOnly = true)
	public Map<UUID, List<PhotoDto>> findPhotosForDishes(Set<UUID> dishIds) {
		if (dishIds == null || dishIds.isEmpty()) {
			return Map.of();
		}
		var photos = repository.findAllByDishIdInAndStatusOrderBySortOrderAsc(dishIds, PhotoStatus.ACTIVE);
		return photos.stream().collect(
				Collectors.groupingBy(
						p -> p.getDish().getId(),
						Collectors.collectingAndThen(Collectors.toList(), mapper::toDto)
				)
		);
	}
}
