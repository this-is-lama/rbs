package my.project.restaurantservice.photo.util;

import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.photo.config.MinioProperties;
import my.project.restaurantservice.photo.service.storage.StorageService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhotoUrlService {

	private static final String PUBLIC_URL_TEMPLATE = "%s/%s/%s";
	private static final int UPLOAD_URL_SECONDS = 120;

	private final MinioProperties minioProperties;
	private final StorageService storageService;

	public String buildPublicUrl(String bucket, String objectKey) {
		String base = stripTrailingSlash(minioProperties.publicBaseUrl());
		return PUBLIC_URL_TEMPLATE.formatted(base, bucket, objectKey);
	}

	public String buildUploadUrl(String bucket, String objectKey) {
		return storageService.presignedUrl(bucket, objectKey, Method.PUT, UPLOAD_URL_SECONDS);
	}

	private static String stripTrailingSlash(String s) {
		return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
	}
}
