package my.project.restaurantservice.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.exception.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService  {

	private final MinioClient minioClient;

	public String save(String bucket, MultipartFile file) {
		var contentType = file.getContentType();
		var key = generateKey(bucket, contentType);
		try {
			minioClient.putObject(
				PutObjectArgs.builder()
						.bucket(bucket)
						.object(generateKey(bucket, file.getContentType()))
						.contentType(file.getContentType())
						.stream(file.getInputStream(), file.getSize(), -1)
						.build()
			);
			return key;
		} catch (Exception e) {
			throw new StorageException("Ошибка сохранения файла");
		}
	}

	public InputStream get(String bucket, String key) {
		return null;
	}

	public void delete(String bucket, String key) {

	}

	private String generateKey(String bucket, String type) {
		UUID id = UUID.randomUUID();
		return String.format("%s/%s%s", bucket, id,type);
	}

}
