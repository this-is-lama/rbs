package my.project.restaurantservice.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.service.storage.StorageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
		markExpired();
		cleanExpired();
	}

	@Transactional
	public void markExpired() {
		Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
		var photos = photoService.findTop500ByStatusAndUploadedAtBefore(PhotoStatus.PENDING, threshold);
		for (var photo : photos) {
			photo.setStatus(PhotoStatus.EXPIRED);
		}
	}

	public void cleanExpired() {
		var photos = photoService.findTop500ByStatus(PhotoStatus.EXPIRED);
		List<UUID> successfullyRemoved = new ArrayList<>(photos.size());
		for (var p : photos) {
			try {
				storageService.removeObject(p.getBucket(), p.getObjectKey());
				successfullyRemoved.add(p.getId());
			} catch (Exception ex) {
				log.warn("Failed to remove object: bucket={}, key={}, id={}",
						p.getBucket(), p.getObjectKey(), p.getId());
			}
		}

		if (!successfullyRemoved.isEmpty()) {
			photoService.deleteAllById(successfullyRemoved);
		}
	}
}
