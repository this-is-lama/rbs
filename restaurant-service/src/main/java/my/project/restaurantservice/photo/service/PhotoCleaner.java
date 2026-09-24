package my.project.restaurantservice.photo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.photo.entity.enums.PhotoStatus;
import my.project.restaurantservice.photo.exception.StorageException;
import my.project.restaurantservice.photo.repository.PhotoRepositoryService;
import my.project.restaurantservice.photo.service.command.PhotoWriteService;
import my.project.restaurantservice.photo.service.storage.StorageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Loggable
@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoCleaner {

	private final PhotoWriteService writeService;
	private final PhotoRepositoryService repositoryService;
	private final StorageService storageService;

	@Scheduled(fixedDelayString = "PT10M")
	public void run() {
		writeService.markExpired();
		cleanByStatus(PhotoStatus.EXPIRED);
		cleanByStatus(PhotoStatus.DELETING);
	}

	private void cleanByStatus(PhotoStatus status) {
		var photos = repositoryService.findTop500ByStatus(status);
		List<UUID> toDelete = new ArrayList<>(photos.size());

		for (var p : photos) {
			try {
				storageService.removeObject(p.getBucket(), p.getObjectKey());
				toDelete.add(p.getId());
			} catch (Exception ex) {
				if (ex instanceof StorageException se && se.isNotFound()) {
					toDelete.add(p.getId());
					continue;
				}
				log.warn("Не удалось удалить объект из хранилища, bucket={}, key={}, id={}",
						p.getBucket(), p.getObjectKey(), p.getId(), ex);
			}
		}

		if (!toDelete.isEmpty()) {
			repositoryService.deleteAllByIdInBatch(toDelete);
			log.info("Удалены записи о фотографиях из базы данных, status={}, count={}", status, toDelete.size());
		}
	}
}