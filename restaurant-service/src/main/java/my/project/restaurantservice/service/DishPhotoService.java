package my.project.restaurantservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.PhotoMetaDto;
import my.project.restaurantservice.entity.DishPhotoEntity;
import my.project.restaurantservice.exception.StorageException;
import my.project.restaurantservice.mapper.DishPhotoMapper;
import my.project.restaurantservice.repository.DishPhotoRepository;
import my.project.restaurantservice.util.KeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishPhotoService {

	private final DishPhotoRepository repository;
	private final DishService dishService;
	private final MinioStorageService storageService;
	private final DishPhotoMapper dishPhotoMapper;
	private final KeyGenerator keyGenerator;

	private static final String DISH_BUCKET = "dish-media";

	public List<UUID> saveAll(UUID dishId, List<MultipartFile> files, List<PhotoMetaDto> metas) {
		validate(files, metas);
		List<UUID> ids = new ArrayList<>();
		var dish = dishService.getRef(dishId);
		for (int i = 0; i < files.size(); i++) {
			var file = files.get(i);
			var meta = metas.get(i);

			var key = keyGenerator.generateKey(dish.getId(), file.getContentType());

			uploadToStorage(key, file);

			DishPhotoEntity photo = dishPhotoMapper.toEntity(meta);
			photo.setObjectKey(key);
			photo.setDish(dish);

			try {
				var id = repository.save(photo).getId();
				ids.add(id);
			} catch (StorageException e) {
				storageService.delete(DISH_BUCKET, key);
			}
		}
		return ids;
	}


	private void uploadToStorage(String key, MultipartFile multipartFile) {
		try {
			storageService.upload(DISH_BUCKET, key, multipartFile);
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
