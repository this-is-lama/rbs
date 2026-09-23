package my.project.restaurantservice.photo.service.query;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.photo.entity.PhotoEntity;
import my.project.restaurantservice.photo.entity.enums.PhotoCategory;
import my.project.restaurantservice.photo.entity.enums.PhotoStatus;
import my.project.restaurantservice.photo.mapper.PhotoMapper;
import my.project.restaurantservice.photo.repository.PhotoRepositoryService;
import my.project.restaurantservice.photo.util.PhotoUrlService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoQueryService {

	private final PhotoRepositoryService repositoryService;
	private final PhotoMapper mapper;
	private final PhotoUrlService urlService;

	@Cacheable(cacheNames = "photosByRestaurantId", key = "#restId", sync = true)
	@Transactional(readOnly = true)
	public List<PhotoDto> getAllByRestaurantId(UUID restId) {
		var photos = repositoryService.findAllByRestaurantIdAndStatus(restId, PhotoStatus.ACTIVE);
		return toDto(photos);
	}

	@Cacheable(cacheNames = "photosByDishId", key = "#dishId", sync = true)
	@Transactional(readOnly = true)
	public List<PhotoDto> getAllByDishId(UUID dishId) {
		var photos = repositoryService.findAllByDishIdAndStatus(dishId, PhotoStatus.ACTIVE);
		return toDto(photos);
	}

	@Transactional(readOnly = true)
	public Map<UUID, PhotoDto> findBannersForRestaurants(Set<UUID> restIds) {
		var photos = repositoryService.findFirstPhotosForRestaurants(restIds, PhotoCategory.BANNER);
		return photos.stream().collect(Collectors.toMap(p -> p.getRestaurant().getId(), this::toDto));
	}

	@Transactional(readOnly = true)
	public Map<UUID, List<PhotoDto>> findPhotosForDishes(Set<UUID> dishIds) {
		if (dishIds == null || dishIds.isEmpty()) {
			return Map.of();
		}

		var photos = repositoryService.findAllByDishIdInAndStatusOrderBySortOrderAsc(dishIds, PhotoStatus.ACTIVE);
		return photos.stream().collect(
				Collectors.groupingBy(
						p -> p.getDish().getId(),
						Collectors.collectingAndThen(Collectors.toList(), this::toDto)
				)
		);
	}

	private PhotoDto toDto(PhotoEntity photo) {
		return mapper.toDto(photo, urlService.buildPublicUrl(photo.getBucket(), photo.getObjectKey()));
	}

	private List<PhotoDto> toDto(List<PhotoEntity> photos) {
		return photos.stream()
				.map(this::toDto)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
