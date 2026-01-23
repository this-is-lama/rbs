package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.PhotoMetaDto;
import my.project.restaurantservice.entity.RestaurantPhotoEntity;
import my.project.restaurantservice.exception.StorageException;
import my.project.restaurantservice.mapper.RestaurantPhotoMapper;
import my.project.restaurantservice.repository.RestaurantPhotoRepository;
import my.project.restaurantservice.util.KeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantPhotoService {

	private final RestaurantPhotoRepository repository;
	private final RestaurantService restaurantService;
	private final MinioStorageService storageService;
	private final RestaurantPhotoMapper restaurantPhotoMapper;
	private final KeyGenerator keyGenerator;

	private static final String RESTAURANT_BUCKET = "restaurant-media";

	public List<UUID> saveAll(UUID restId, List<MultipartFile> files, List<PhotoMetaDto> metaList) {
		validate(files, metaList);
		List<UUID> ids = new ArrayList<>();
		var restaurant = restaurantService.getRef(restId);
		for (int i = 0; i < files.size(); i++) {
			var file = files.get(i);
			var meta = metaList.get(i);

			var key = keyGenerator.generateKey(restaurant.getId(), file.getContentType());

			uploadToStorage(key, file);

			RestaurantPhotoEntity photo = restaurantPhotoMapper.toEntity(meta);
			photo.setObjectKey(key);
			photo.setRestaurant(restaurant);

			try {
				var id = repository.save(photo).getId();
				ids.add(id);
			} catch (StorageException e) {
				storageService.delete(RESTAURANT_BUCKET, key);
			}
		}
		return ids;
	}


	private void uploadToStorage(String key, MultipartFile multipartFile) {
		try {
			storageService.upload(RESTAURANT_BUCKET, key, multipartFile);
		} catch (StorageException e) {
			log.error(e.getMessage());
			throw new StorageException(e.getMessage());
		}
	}

	private void validate(List<MultipartFile> files, List<PhotoMetaDto> metaList) {
		if (files == null || files.isEmpty()) {
			throw new IllegalArgumentException("Files must not be empty");
		}

		if (metaList == null || metaList.isEmpty()) {
			throw new IllegalArgumentException("Photo meta must not be empty");
		}

		if (files.size() != metaList.size()) {
			throw new IllegalArgumentException(
					"Files count does not match meta count"
			);
		}
	}
}
