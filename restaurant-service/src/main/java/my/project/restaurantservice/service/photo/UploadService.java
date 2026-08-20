package my.project.restaurantservice.service.photo;

import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.NotFoundException;
import my.project.common.exception.ValidationException;
import my.project.restaurantservice.dto.photo.PhotoConfirmRequest;
import my.project.restaurantservice.dto.photo.PhotoConfirmResponse;
import my.project.restaurantservice.dto.photo.PhotoUploadRequest;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.mapper.PhotoMapper;
import my.project.restaurantservice.service.manager.ManagerAccessService;
import my.project.restaurantservice.service.photo.provider.ContainerType;
import my.project.restaurantservice.service.photo.provider.PhotoContainerProvider;
import my.project.restaurantservice.service.photo.provider.ProviderContext;
import my.project.restaurantservice.service.storage.StorageService;
import my.project.restaurantservice.util.KeyGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final int SECONDS_DURATION = 120;

    private final StorageService storageService;
    private final ManagerAccessService managerAccessService;
    private final PhotoService photoService;
    private final PhotoReadService photoReadService;
    private final PhotoMapper photoMapper;
    private final KeyGenerator keyGenerator;
    private final Map<ContainerType, PhotoContainerProvider> providers;

    @Transactional
    public List<PhotoConfirmResponse> pendingUpload(ContainerType type, UUID containerId,
                                                    List<PhotoUploadRequest> dto, Authentication auth) {
        log.info("Подготовка загрузки фотографий, type={}, containerId={}, count={}", type, containerId, dto.size());

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
        log.info("Фотографии успешно подготовлены к загрузке, type={}, containerId={}, count={}",
                type, containerId, saved.size());

        return createPresignedUpload(bucket, saved);
    }

    @Transactional
    public List<UUID> confirmUpload(ContainerType type, UUID containerId,
                                    List<PhotoConfirmRequest> uploaded, Authentication auth) {
        log.info("Подтверждение загрузки фотографий, type={}, containerId={}, count={}",
                type, containerId, uploaded.size());

        ProviderContext context = checkAccessAndGetContext(type, containerId, auth);
        var bucket = context.bucket();

        List<UUID> ids = new ArrayList<>();
        for (PhotoConfirmRequest dto : uploaded) {
            if (!storageService.objectExists(bucket, dto.objectKey())) {
                log.warn("Объект не найден в хранилище при подтверждении, bucket={}, objectKey={}",
                        bucket, dto.objectKey());
                continue;
            }

            PhotoEntity photo = photoReadService.findPending(dto.id(), dto.objectKey());
            assertBelongsToContainer(photo, type, containerId, bucket);
            photo.confirm();

            ids.add(photo.getId());
        }

        evictPhotosCache(type, containerId);

        log.info("Загрузка фотографий подтверждена, type={}, containerId={}, confirmedCount={}",
                type, containerId, ids.size());
        return ids;
    }

    @Transactional
    public void delete(ContainerType type, UUID containerId,
                       Set<UUID> ids, Authentication auth) {
        log.info("Запрос на удаление фотографий, type={}, containerId={}, count={}",
                type, containerId, ids.size());

        ProviderContext context = checkAccessAndGetContext(type, containerId, auth);
        var bucket = context.bucket();

        var photos = photoReadService.findAllByIdIn(ids);
        photos.forEach(p -> assertBelongsToContainer(p, type, containerId, bucket));
        photoService.markDeleting(photos);

        evictPhotosCache(type, containerId);

        log.info("Фотографии помечены на удаление, type={}, containerId={}, count={}",
                type, containerId, ids.size());
    }

    private PhotoContainerProvider provider(ContainerType type) {
        PhotoContainerProvider p = providers.get(type);
        if (p == null) {
            log.warn("Неподдерживаемый тип контейнера для фотографий: {}", type);
            throw new ValidationException(CommonErrorCode.BAD_REQUEST, "restaurant.photo.unsupported-owner-type", type);
        }
        return p;
    }

    private List<PhotoConfirmResponse> createPresignedUpload(String bucket, List<PhotoEntity> photos) {
        List<PhotoConfirmResponse> dto = photoMapper.toResponse(photos);
        for (PhotoConfirmResponse p : dto) {
            String key = p.getObjectKey();
            p.setPresignedUrl(storageService.presignedUrl(bucket, key, Method.PUT, SECONDS_DURATION));
        }
        return dto;
    }

    private ProviderContext checkAccessAndGetContext(ContainerType type, UUID containerId, Authentication auth) {
        ProviderContext context = provider(type).context(containerId);
        managerAccessService.checkAccess(context.accessContainerId(), auth);
        return context;
    }

    private void assertBelongsToContainer(PhotoEntity photo, ContainerType type,
                                          UUID containerId, String expectedBucket) {
        if (!photo.isOwnContainerAndBucket(type, containerId, expectedBucket)) {
            log.warn("Фотография не принадлежит указанному контейнеру, photoId={}, type={}, containerId={}",
                    photo.getId(), type, containerId);
            throw new NotFoundException("restaurant.photo.not-found", photo.getId());
        }
    }

    private void evictPhotosCache(ContainerType type, UUID containerId) {
        switch (type) {
            case DISHES -> photoReadService.evictPhotosByDishId(containerId);
            case RESTAURANTS -> photoReadService.evictPhotosByRestaurantId(containerId);
        }
    }
}