package my.project.restaurantservice.photo.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.photo.dto.PhotoConfirmRequest;
import my.project.restaurantservice.photo.dto.PhotoConfirmResponse;
import my.project.restaurantservice.photo.dto.PhotoUploadRequest;
import my.project.restaurantservice.photo.mapper.PhotoMapper;
import my.project.restaurantservice.photo.service.provider.ContainerType;
import my.project.restaurantservice.photo.service.provider.PhotoContainerResolver;
import my.project.restaurantservice.photo.service.provider.ProviderContext;
import my.project.restaurantservice.photo.service.storage.StorageService;
import my.project.restaurantservice.photo.util.PhotoUrlService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoCommandService {

	private final PhotoWriteService writeService;
	private final StorageService storageService;
	private final ManagerAccessService managerAccessService;
	private final PhotoContainerResolver containerResolver;
	private final PhotoMapper mapper;
	private final PhotoUrlService urlService;

	public List<PhotoConfirmResponse> pendingUpload(ContainerType type, UUID containerId,
													List<PhotoUploadRequest> dto, Authentication auth) {
		String bucket = checkAccessAndGetBucket(type, containerId, auth);

		return writeService.savePending(type, containerId, bucket, dto).stream()
				.map(p -> mapper.toResponse(
						p,
						urlService.buildUploadUrl(bucket, p.getObjectKey()),
						urlService.buildPublicUrl(bucket, p.getObjectKey())
				))
				.toList();
	}

	public List<UUID> confirmUpload(ContainerType type, UUID containerId,
									List<PhotoConfirmRequest> uploaded, Authentication auth) {
		String bucket = checkAccessAndGetBucket(type, containerId, auth);

		List<PhotoConfirmRequest> existing = uploaded.stream()
				.filter(dto -> existsInStorage(bucket, dto))
				.toList();

		return writeService.confirm(type, containerId, bucket, existing);
	}

	public void delete(ContainerType type, UUID containerId, Set<UUID> ids, Authentication auth) {
		String bucket = checkAccessAndGetBucket(type, containerId, auth);
		writeService.markDeleting(type, containerId, bucket, ids);
	}

	private boolean existsInStorage(String bucket, PhotoConfirmRequest dto) {
		if (storageService.objectExists(bucket, dto.objectKey())) {
			return true;
		}
		log.warn("Объект не найден в хранилище при подтверждении, bucket={}, objectKey={}", bucket, dto.objectKey());
		return false;
	}

	private String checkAccessAndGetBucket(ContainerType type, UUID containerId, Authentication auth) {
		ProviderContext context = containerResolver.context(type, containerId);
		managerAccessService.checkAccess(context.accessContainerId(), auth);
		return context.bucket();
	}
}
