package my.project.restaurantservice.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.exception.StorageException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Profile("dev")
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements ApplicationRunner {

	private final MinioClient minioClient;
	private final MinioProperties minioProperties;

	@Override
	public void run(ApplicationArguments args) {
		var buckets = minioProperties.buckets();
		if (buckets.isEmpty()) {
			return;
		}
		buckets.forEach(bucket -> {
			if (!existBucket(bucket)) {
				createBucket(bucket);
			}
		});
	}

	private boolean existBucket(String bucketName) {
		try {
			return minioClient.bucketExists(
					BucketExistsArgs.builder()
							.bucket(bucketName)
							.build()
			);
		} catch (Exception e) {
			throw new StorageException("Ошибка проверки бакета");
		}
	}

	private void createBucket(String bucketName) {
		try {
			minioClient.makeBucket(
					MakeBucketArgs.builder()
							.bucket(bucketName)
							.extraQueryParams(Map.of())
							.build()
			);
			log.info("бакет {} успешно создан", bucketName);
		} catch (Exception e) {
			throw new StorageException("Ошибка создания бакета");
		}
	}
}
