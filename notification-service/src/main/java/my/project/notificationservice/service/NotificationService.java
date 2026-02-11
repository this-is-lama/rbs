package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.events.BookingCreatedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final MailSenderService mailSenderService;
	private final MessageStorageService messageStorageService;

	public void send(BookingCreatedEvent event) {
		var messageId = event.bookingId();

		if (!messageStorageService.save(event)) {
			log.info("Duplicate event skipped bookingId={}", messageId);
			return;
		}

		try {
			mailSenderService.sendMessage(event);
			messageStorageService.markStatus(messageId, MessageEntity::done);
		} catch (MessagingException e) {
			log.error("Failed to send notification bookingId={}", messageId, e);
			messageStorageService.markStatus(messageId, MessageEntity::processing);
		}
	}

}
