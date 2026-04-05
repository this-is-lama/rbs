package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

	private final PhotoRepository repository;

	@Transactional
	public List<PhotoEntity> saveAll(List<PhotoEntity> photos) {
		log.info("Сохранение фотографий, count={}", photos.size());
		return repository.saveAll(photos);
	}

	@Transactional
	public void deleteAllById(List<UUID> ids) {
		log.info("Удаление фотографий из базы данных, count={}", ids.size());
		repository.deleteAllById(ids);
	}

	@Transactional
	public void markExpired() {
		Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
		var photos = repository.findTop500ByStatusAndUploadedAtBefore(PhotoStatus.PENDING, threshold);
		photos.forEach(PhotoEntity::expired);
		if (!photos.isEmpty()) {
			log.info("Фотографии помечены как EXPIRED, count={}", photos.size());
		}
	}

	@Transactional
	public void markDeleting(List<PhotoEntity> photos) {
		photos.forEach(PhotoEntity::deleting);
		if (!photos.isEmpty()) {
			log.info("Фотографии помечены как DELETING, count={}", photos.size());
		}
	}
}