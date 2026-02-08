package my.project.restaurantservice.service.photo;

import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.NotFoundException;
import my.project.common.exception.ValidationException;
import my.project.restaurantservice.dto.photo.PhotoConfirmRequest;
import my.project.restaurantservice.dto.photo.PhotoResponse;
import my.project.restaurantservice.dto.photo.PhotoUploadRequest;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.mapper.PhotoMapper;
import my.project.restaurantservice.service.ManagerService;
import my.project.restaurantservice.service.photo.provider.ContainerType;
import my.project.restaurantservice.service.photo.provider.PhotoContainerProvider;
import my.project.restaurantservice.service.photo.provider.ProviderContext;
import my.project.restaurantservice.service.storage.StorageService;
import my.project.restaurantservice.util.KeyGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final int SECONDS_DURATION = 120;

    private final StorageService storageService;
    private final ManagerService managerService;
    private final PhotoService photoService;
    private final PhotoMapper photoMapper;
    private final KeyGenerator keyGenerator;

    private final Map<ContainerType, PhotoContainerProvider> providers;

    @Transactional
    public List<PhotoResponse> pendingUpload(ContainerType type, UUID containerId,
                                             List<PhotoUploadRequest> dto, Authentication auth) {
        ProviderContext context = checkAccessAndGetContext(type, containerId, auth);
        var bucket = context.bucket();
        var container = context.container();

        List<PhotoEntity> photos = photoMapper.toEntity(dto);
        for (PhotoEntity p : photos) {
            p.setBucket(bucket);
            p.setObjectKey(keyGenerator.generateKey(container.getId(), p.getContentType()));
            container.addPhoto(p);
        }

        List<PhotoEntity> saved = photoService.saveAll(photos);
        return createPresignedUpload(bucket, saved);
    }

    @Transactional
    public List<UUID> confirmUpload(ContainerType type, UUID containerId,
                                    List<PhotoConfirmRequest> uploaded, Authentication auth) {
        ProviderContext context = checkAccessAndGetContext(type, containerId, auth);
        var bucket = context.bucket();
        List<UUID> ids = new ArrayList<>();
        for (PhotoConfirmRequest dto : uploaded) {
            if (!storageService.objectExists(bucket, dto.objectKey())) {
                continue;
            }

            PhotoEntity photo = photoService.findPending(dto.id(), dto.objectKey());
            assertBelongsToContainer(photo, type, containerId, bucket);
            photo.confirm();
            ids.add(photo.getId());
        }
        return ids;
    }

    @Transactional
    public void delete(ContainerType type, UUID containerId,
                       Set<UUID> ids, Authentication auth) {
        ProviderContext context = checkAccessAndGetContext(type, containerId, auth);
        var bucket = context.bucket();

        var photos = photoService.findAllByIdIn(ids);
        photos.forEach(p -> assertBelongsToContainer(p, type, containerId, bucket));
        photoService.markDeleting(photos);
    }

    private PhotoContainerProvider provider(ContainerType type) {
        PhotoContainerProvider p = providers.get(type);
        if (p == null) {
            throw new ValidationException(CommonErrorCode.BAD_REQUEST, "restaurant.photo.unsupported-owner-type", type);
        }
        return p;
    }

    private List<PhotoResponse> createPresignedUpload(String bucket, List<PhotoEntity> photos) {
        List<PhotoResponse> dto = photoMapper.toDto(photos);
        for (PhotoResponse p : dto) {
            String key = p.getObjectKey();
            p.setPresignedUrl(storageService.presignedUrl(bucket, key, Method.PUT, SECONDS_DURATION));
        }
        return dto;
    }

    private ProviderContext checkAccessAndGetContext(ContainerType type, UUID containerId, Authentication auth) {
        ProviderContext context = provider(type).context(containerId);
        managerService.checkAccess(context.accessContainerId(), auth);
        return context;
    }

    private void assertBelongsToContainer(PhotoEntity photo, ContainerType type,
                                          UUID containerId, String expectedBucket) {
        if (!photo.isOwnContainerAndBucket(type, containerId, expectedBucket)) {
            throw new NotFoundException("restaurant.photo.not-found", photo.getId());
        }
    }


}
