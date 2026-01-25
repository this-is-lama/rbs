package my.project.restaurantservice.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.exception.StorageException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

	private final MinioClient minioClient;

	public String presignedUrl(String bucket, String objectKey, Method method, int seconds) {
		try {
			return minioClient.getPresignedObjectUrl(
						GetPresignedObjectUrlArgs.builder()
						.method(method)
						.bucket(bucket)
						.object(objectKey)
						.expiry(seconds)
						.build()
			);
		} catch (Exception e) {
			log.error("Presign Post URL failed. bucket={}, key={}", bucket, objectKey, e);
			throw new StorageException("Ошибка генерации ссылки загрузки");
		}
	}

	public boolean objectExists(String bucket, String objectKey) {
		try {
			minioClient.statObject(
					StatObjectArgs.builder()
							.bucket(bucket)
							.object(objectKey)
							.build()
			);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void removeObject(String bucket, String objectKey) {
		try {
			minioClient.removeObject(
					RemoveObjectArgs.builder()
							.bucket(bucket)
							.object(objectKey)
							.build()
			);
		} catch (Exception e) {
			log.error("Remove object failed. bucket={}, objectKey={}", bucket, objectKey);
			throw new StorageException("Remove object failed. bucket=" + bucket + ", objectKey=" + objectKey);
		}
	}


}
