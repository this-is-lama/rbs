package my.project.restaurantservice.photo.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.photo.dto.PhotoConfirmRequest;
import my.project.restaurantservice.photo.dto.PhotoUploadRequest;
import my.project.restaurantservice.photo.entity.PhotoContainer;
import my.project.restaurantservice.photo.entity.PhotoEntity;
import my.project.restaurantservice.photo.entity.enums.PhotoStatus;
import my.project.restaurantservice.photo.mapper.PhotoMapper;
import my.project.restaurantservice.photo.repository.PhotoRepositoryService;
import my.project.restaurantservice.photo.service.provider.ContainerType;
import my.project.restaurantservice.photo.service.provider.PhotoContainerResolver;
import my.project.restaurantservice.photo.util.KeyGenerator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoWriteService {

	private final PhotoRepositoryService repositoryService;
	private final PhotoMapper mapper;
	private final PhotoContainerResolver containerResolver;

	@Transactional
	public List<PhotoEntity> savePending(ContainerType type, UUID containerId, String bucket,
										 List<PhotoUploadRequest> requests) {
		PhotoContainer container = containerResolver.getRef(type, containerId);

		List<PhotoEntity> photos = mapper.toEntity(requests);
		for (PhotoEntity p : photos) {
			p.setBucket(bucket);
			p.setObjectKey(KeyGenerator.generateKey(container.getId(), p.getContentType()));
			container.addPhoto(p);
		}

		return repositoryService.saveAll(photos);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "photosByRestaurantId", key = "#containerId", condition = "#type.name() == 'RESTAURANTS'"),
			@CacheEvict(cacheNames = "photosByDishId", key = "#containerId", condition = "#type.name() == 'DISHES'")
	})
	@Transactional
	public List<UUID> confirm(ContainerType type, UUID containerId, String bucket,
							  List<PhotoConfirmRequest> uploaded) {
		List<UUID> ids = new ArrayList<>();
		for (PhotoConfirmRequest dto : uploaded) {
			PhotoEntity photo = repositoryService.getByIdAndObjectKeyAndStatus(dto.id(), dto.objectKey(), PhotoStatus.PENDING);
			assertBelongsToContainer(photo, type, containerId, bucket);
			photo.confirm();

			ids.add(photo.getId());
		}
		return ids;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "photosByRestaurantId", key = "#containerId", condition = "#type.name() == 'RESTAURANTS'"),
			@CacheEvict(cacheNames = "photosByDishId", key = "#containerId", condition = "#type.name() == 'DISHES'")
	})
	@Transactional
	public void markDeleting(ContainerType type, UUID containerId, String bucket, Set<UUID> ids) {
		var photos = repositoryService.getAllByIdIn(ids);
		photos.forEach(p -> assertBelongsToContainer(p, type, containerId, bucket));
		photos.forEach(PhotoEntity::deleting);

		if (!photos.isEmpty()) {
			log.info("Фотографии помечены как DELETING, count={}", photos.size());
		}
	}

	@Transactional
	public void markExpired() {
		Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
		var photos = repositoryService.findTop500ByStatusAndUploadedAtBefore(PhotoStatus.PENDING, threshold);
		photos.forEach(PhotoEntity::expired);
		if (!photos.isEmpty()) {
			log.info("Фотографии помечены как EXPIRED, count={}", photos.size());
		}
	}

	private void assertBelongsToContainer(PhotoEntity photo, ContainerType type, UUID containerId, String expectedBucket) {
		if (!photo.isOwnContainerAndBucket(type, containerId, expectedBucket)) {
			log.warn("Фотография не принадлежит указанному контейнеру, photoId={}, type={}, containerId={}", photo.getId(), type, containerId);
			throw new NotFoundException("restaurant.photo.not-found", photo.getId());
		}
	}
}
