package my.project.restaurantservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

	void save(String bucket, MultipartFile file);

	InputStream get(String bucket, String key);

	void delete(String bucket, String key);
}
