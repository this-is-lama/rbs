package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.photo.PhotoConfirmRequest;
import my.project.restaurantservice.dto.photo.PhotoResponse;
import my.project.restaurantservice.dto.photo.PhotoUploadRequest;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.PhotoContainer;
import my.project.restaurantservice.mapper.PhotoMapper;
import my.project.restaurantservice.service.storage.StorageService;
import my.project.restaurantservice.util.KeyGenerator;
import my.project.restaurantservice.util.PhotoUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final int SECONDS_DURATION = 120;

    private final StorageService storageService;

    private final PhotoService photoService;
    private final PhotoMapper photoMapper;

    private final KeyGenerator keyGenerator;
    private final PhotoUrlService photoUrlService;

    private final Map<OwnerType, PhotoContainerProvider> providers;

    @Transactional
    public List<PhotoResponse> pendingUpload(OwnerType type, UUID ownerId, List<PhotoUploadRequest> dto) {
        PhotoContainerProvider provider = provider(type);
        String bucket = provider.bucket();
        PhotoContainer owner = provider.getRef(ownerId);

        List<PhotoEntity> photos = photoMapper.toEntity(dto);
        for (PhotoEntity p : photos) {
            p.setBucket(bucket);
            p.setObjectKey(keyGenerator.generateKey(owner.getId(), p.getContentType()));
            owner.addPhoto(p);
        }

        List<PhotoEntity> saved = photoService.saveAll(photos);
        return createPresignedUpload(bucket, saved);
    }

    @Transactional
    public List<UUID> confirmUpload(OwnerType type, List<PhotoConfirmRequest> uploaded) {
        if (uploaded == null || uploaded.isEmpty()) return List.of();

        String bucket = provider(type).bucket();

        List<UUID> ids = new ArrayList<>();
        for (PhotoConfirmRequest dto : uploaded) {
            if (!storageService.objectExists(bucket, dto.objectKey())) {
                continue;
            }

            PhotoEntity photo = photoService.findByIdAndObjectKey(dto.id(), dto.objectKey());
            photo.confirm();
            ids.add(photo.getId());
        }
        return ids;
    }

    private PhotoContainerProvider provider(OwnerType type) {
        PhotoContainerProvider p = providers.get(type);
        if (p == null) throw new IllegalArgumentException("Unsupported owner type: " + type);
        return p;
    }

    private List<PhotoResponse> createPresignedUpload(String bucket, List<PhotoEntity> photos) {
        List<PhotoResponse> dto = photoMapper.toDto(photos);
        for (PhotoResponse p : dto) {
            String key = p.getObjectKey();
            p.setPublicUrl(photoUrlService.buildPublicUrl(bucket, key));
            p.setPresignedUrl(storageService.presignedUrl(bucket, key, io.minio.http.Method.PUT, SECONDS_DURATION));
        }
        return dto;
    }
}
