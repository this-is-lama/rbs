package my.project.restaurantservice.service.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.CommonErrorCode;
import my.project.restaurantservice.exception.StorageException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

	private final MinioClient minioClient;

	public String presignedUrl(String bucket, String objectKey, Method method, int seconds) {
		try {
			log.debug("Генерация presigned URL, bucket={}, objectKey={}, method={}", bucket, objectKey, method);
			return minioClient.getPresignedObjectUrl(
					GetPresignedObjectUrlArgs.builder()
							.method(method)
							.bucket(bucket)
							.object(objectKey)
							.expiry(seconds)
							.build()
			);
		} catch (Exception e) {
			log.error("Ошибка генерации presigned URL, bucket={}, objectKey={}", bucket, objectKey, e);
			throw new StorageException("restaurant.storage.presignedurl-generation-error");
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
			log.debug("Объект найден в хранилище, bucket={}, objectKey={}", bucket, objectKey);
			return true;
		} catch (ErrorResponseException e) {
			String code = e.errorResponse().code();

			if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code)) {
				log.debug("Объект не найден в хранилище, bucket={}, objectKey={}", bucket, objectKey);
				return false;
			}

			log.warn("Ошибка проверки объекта в MinIO, bucket={}, objectKey={}, code={}",
					bucket, objectKey, code, e);
			throw new StorageException("restaurant.storage.stat-error", objectKey, bucket);
		} catch (Exception e) {
			log.warn("Ошибка проверки объекта в хранилище, bucket={}, objectKey={}", bucket, objectKey, e);
			throw new StorageException("restaurant.storage.stat-error", objectKey, bucket);
		}
	}

	public void removeObject(String bucket, String objectKey) {
		try {
			log.debug("Удаление объекта из хранилища, bucket={}, objectKey={}", bucket, objectKey);
			minioClient.removeObject(
					RemoveObjectArgs.builder()
							.bucket(bucket)
							.object(objectKey)
							.build()
			);
		} catch (ErrorResponseException e) {
			if ("NoSuchKey".equals(e.errorResponse().code())) {
				log.debug("Объект для удаления не найден, bucket={}, objectKey={}", bucket, objectKey);
				throw new StorageException(CommonErrorCode.NOT_FOUND, "restaurant.storage.not-found", objectKey, bucket);
			}
			log.error("Ошибка удаления объекта из MinIO, bucket={}, objectKey={}, code={}",
					bucket, objectKey, e.errorResponse().code(), e);
			throw new StorageException("restaurant.storage.delete-error", objectKey);
		} catch (Exception e) {
			log.error("Ошибка удаления объекта из хранилища, bucket={}, objectKey={}", bucket, objectKey, e);
			throw new StorageException("restaurant.storage.delete-error", objectKey);
		}
	}
}