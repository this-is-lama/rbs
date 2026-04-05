package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.CommonErrorCode;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.exception.StorageException;
import my.project.restaurantservice.service.storage.StorageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoCleaner {

	private final PhotoService photoService;
	private final PhotoReadService photoReadService;
	private final StorageService storageService;

	@Scheduled(fixedDelayString = "PT10M")
	public void run() {
		log.info("Запущена фоновая очистка фотографий");
		photoService.markExpired();
		cleanByStatus(PhotoStatus.EXPIRED);
		cleanByStatus(PhotoStatus.DELETING);
		log.info("Фоновая очистка фотографий завершена");
	}

	private void cleanByStatus(PhotoStatus status) {
		var photos = photoReadService.findTop500ByStatus(status);
		List<UUID> toDelete = new ArrayList<>(photos.size());

		for (var p : photos) {
			try {
				storageService.removeObject(p.getBucket(), p.getObjectKey());
				toDelete.add(p.getId());
			} catch (Exception ex) {
				if (isNotFound(ex)) {
					toDelete.add(p.getId());
					continue;
				}
				log.warn("Не удалось удалить объект из хранилища, bucket={}, key={}, id={}",
						p.getBucket(), p.getObjectKey(), p.getId(), ex);
			}
		}

		if (!toDelete.isEmpty()) {
			photoService.deleteAllById(toDelete);
			log.info("Удалены записи о фотографиях из базы данных, status={}, count={}", status, toDelete.size());
		}
	}

	private boolean isNotFound(Exception ex) {
		return ex instanceof StorageException se && se.getCode() == CommonErrorCode.NOT_FOUND;
	}
}