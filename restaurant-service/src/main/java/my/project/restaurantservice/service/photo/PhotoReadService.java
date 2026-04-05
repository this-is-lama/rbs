package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoReadService {

	private final PhotoRepository repository;
	private final PhotoMapper mapper;

	@Cacheable(cacheNames = "photosByRestaurantId", key = "#restId", sync = true)
	@Transactional(readOnly = true)
	public List<PhotoDto> getAllByRestaurantId(UUID restId) {
		log.debug("Получение фотографий ресторана, restId={}", restId);
		var photos = repository.findAllByRestaurantIdAndStatus(restId, PhotoStatus.ACTIVE);
		return mapper.toDto(photos);
	}

	@CacheEvict(cacheNames = "photosByRestaurantId", key = "#restId")
	public void evictPhotosByRestaurantId(UUID restId) {
		log.debug("Очистка кэша фотографий ресторана, restId={}", restId);
	}

	@Cacheable(cacheNames = "photosByDishId", key = "#dishId", sync = true)
	@Transactional(readOnly = true)
	public List<PhotoDto> getAllByDishId(UUID dishId) {
		log.debug("Получение фотографий блюда, dishId={}", dishId);
		var photos = repository.findAllByDishIdAndStatus(dishId, PhotoStatus.ACTIVE);
		return mapper.toDto(photos);
	}

	@CacheEvict(cacheNames = "photosByDishId", key = "#dishId")
	public void evictPhotosByDishId(UUID dishId) {
		log.debug("Очистка кэша фотографий блюда, dishId={}", dishId);
	}

	@Transactional(readOnly = true)
	public PhotoEntity findPending(UUID id, String objectKey) {
		log.debug("Поиск ожидающей фотографии, photoId={}, objectKey={}", id, objectKey);
		return repository.findByIdAndObjectKeyAndStatus(id, objectKey, PhotoStatus.PENDING)
				.orElseThrow(() -> new NotFoundException("restaurant.photo.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<PhotoEntity> findTop500ByStatus(PhotoStatus status) {
		log.debug("Получение фотографий по статусу, status={}", status);
		return repository.findTop500ByStatus(status);
	}

	@Transactional
	public List<PhotoEntity> findAllByIdIn(Set<UUID> ids) {
		log.debug("Получение списка фотографий по id, count={}", ids.size());
		var photos = repository.findAllByIdIn(ids);
		if (photos.size() != ids.size()) {
			throw new NotFoundException("restaurant.photo.not-found", ids);
		}
		return photos;
	}

	@Transactional(readOnly = true)
	public Map<UUID, PhotoEntity> findBannersForRestaurants(Set<UUID> restIds) {
		log.debug("Получение баннеров для ресторанов, count={}", restIds.size());
		var photos = repository.findFirstPhotosForRestaurants(restIds, PhotoCategory.BANNER);
		return photos.stream().collect(Collectors.toMap(p -> p.getRestaurant().getId(), p -> p));
	}

	@Transactional(readOnly = true)
	public Map<UUID, List<PhotoDto>> findPhotosForDishes(Set<UUID> dishIds) {
		log.debug("Получение фотографий для списка блюд, count={}", dishIds == null ? 0 : dishIds.size());

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