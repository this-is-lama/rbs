package my.project.notificationservice.service;

import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.entity.MessageStatus;
import my.project.notificationservice.events.BookingNotificationEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface MessageStorageService {

	default UUID messageId(BookingNotificationEvent event) {
		String value = event.messageType().name() + ":" + event.bookingId();
		return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
	}

	boolean save(BookingNotificationEvent event);

	void markStatus(UUID messageId, Consumer<MessageEntity> action);

	List<MessageEntity> getWorkBatch(int maxAttempts, int stuckMinutes);

	long cleanDone(Instant time);
}
