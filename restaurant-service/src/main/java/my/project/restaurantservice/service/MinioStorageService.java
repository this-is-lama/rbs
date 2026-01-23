package my.project.restaurantservice.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.exception.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioStorageService  {

	private final MinioClient minioClient;

	public void upload(String bucket, String key, MultipartFile file) {
		String contentType = file.getContentType();
		try {
			minioClient.putObject(
				PutObjectArgs.builder()
						.bucket(bucket)
						.object(key)
						.contentType(contentType)
						.stream(file.getInputStream(), file.getSize(), -1)
						.build()
			);
		} catch (Exception e) {
			delete(bucket, key);
			throw new StorageException("Ошибка сохранения файла");
		}
	}

	public void delete(String bucket, String key) {
		try {
			minioClient.removeObject(
					RemoveObjectArgs.builder()
							.bucket(bucket)
							.object(key)
							.build()
			);
		} catch (Exception e) {
			throw new StorageException("Ошибка удаления файла");
		}
	}

}
