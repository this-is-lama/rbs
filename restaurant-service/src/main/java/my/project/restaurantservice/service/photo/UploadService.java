package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.config.minio.MinioProperties;
import my.project.restaurantservice.dto.PhotoDto;
import my.project.restaurantservice.entity.PhotoContainer;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.exception.StorageException;
import my.project.restaurantservice.mapper.PhotoMapper;
import my.project.restaurantservice.service.storage.StorageService;
import my.project.restaurantservice.util.KeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final String PUBLIC_URL_TEMPLATE = "%s/%s/%s";
    private static final int SECONDS_DURATION = 120;

    private final MinioProperties minioProperties;

    private final StorageService storageService;

    private final PhotoService photoService;
    private final PhotoMapper photoMapper;

    private final KeyGenerator keyGenerator;

    private final Map<OwnerType, PhotoContainerProvider> providers;


    @Transactional
    public List<PhotoDto> pendingUpload(OwnerType type, UUID ownerId, List<PhotoDto> dto) {
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
    public List<UUID> confirmUpload(OwnerType type, List<PhotoDto> uploaded) {
        if (uploaded == null || uploaded.isEmpty()) return List.of();

        String bucket = provider(type).bucket();

        List<UUID> ids = new ArrayList<>();
        for (PhotoDto dto : uploaded) {
            if (!storageService.objectExists(bucket, dto.getObjectKey())) {
                continue;
            }

            PhotoEntity photo = photoService.findByIdAndObjectKey(dto.getId(), dto.getObjectKey());
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

    private List<PhotoDto> createPresignedUpload(String bucket, List<PhotoEntity> photos) {
        List<PhotoDto> dto = photoMapper.toDto(photos);
        for (PhotoDto p : dto) {
            String key = p.getObjectKey();
            p.setPublicUrl(buildPublicUrl(bucket, key));
            p.setPresignedURL(storageService.presignedUrl(bucket, key, io.minio.http.Method.PUT, SECONDS_DURATION));
        }
        return dto;
    }

    private String buildPublicUrl(String bucket, String objectKey) {
        String base = stripTrailingSlash(minioProperties.publicBaseUrl());
        return PUBLIC_URL_TEMPLATE.formatted(base, bucket, objectKey);
    }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
