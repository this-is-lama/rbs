package my.project.notificationservice.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.events.BookingNotificationEvent;
import my.project.notificationservice.service.NotificationService;
import my.project.notificationservice.service.SenderService;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final SenderService mailSenderService;
	private final MessageStorageServiceImpl storageService;

	public void send(BookingNotificationEvent event) {
		var messageId = storageService.messageId(event);

		if (!storageService.save(event)) {
			log.info("Дубликат события пропущен, messageId={}, bookingId={}, messageType={}", messageId, event.bookingId(), event.messageType());
			return;
		}

		try {
			mailSenderService.sendMessage(event);
			storageService.markStatus(messageId, MessageEntity::done);
		} catch (MessagingException | MailException e) {
			log.error("Не удалось отправить уведомление, messageId={}, bookingId={}, messageType={}", messageId, event.bookingId(), event.messageType(), e);
			storageService.markStatus(messageId, MessageEntity::processing);
		}
	}
}