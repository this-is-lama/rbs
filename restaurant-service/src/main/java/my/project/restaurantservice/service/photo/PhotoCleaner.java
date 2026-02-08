package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.CommonErrorCode;
import my.project.common.exception.NotFoundException;
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
	private final StorageService storageService;

	@Scheduled(fixedDelayString = "PT10M")
	public void run() {
		photoService.markExpired();
		cleanByStatus(PhotoStatus.EXPIRED);
		cleanByStatus(PhotoStatus.DELETING);
	}

	private void cleanByStatus(PhotoStatus status) {
		var photos = photoService.findTop500ByStatus(status);
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
				log.warn("Failed to remove object: bucket={}, key={}, id={}",
						p.getBucket(), p.getObjectKey(), p.getId(), ex);
			}
		}
		if (!toDelete.isEmpty()) {
			photoService.deleteAllById(toDelete);
		}
	}


	private boolean isNotFound(Exception ex) {
		return ex instanceof StorageException se && se.getCode() == CommonErrorCode.NOT_FOUND;
	}
}
