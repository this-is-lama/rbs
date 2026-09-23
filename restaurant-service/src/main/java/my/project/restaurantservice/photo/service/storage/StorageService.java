package my.project.restaurantservice.photo.service.storage;

import io.minio.http.Method;

public interface StorageService {

	String presignedUrl(String bucket, String objectKey, Method method, int seconds);

	boolean objectExists(String bucket, String objectKey);

	void removeObject(String bucket, String objectKey);
}
