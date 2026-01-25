package my.project.restaurantservice.util;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.config.minio.MinioProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhotoUrlService {

	private static final String PUBLIC_URL_TEMPLATE = "%s/%s/%s";

	public final MinioProperties minioProperties;

	public String buildPublicUrl(String bucket, String objectKey) {
		String base = stripTrailingSlash(minioProperties.publicBaseUrl());
		return PUBLIC_URL_TEMPLATE.formatted(base, bucket, objectKey);
	}

	private static String stripTrailingSlash(String s) {
		return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
	}
}
