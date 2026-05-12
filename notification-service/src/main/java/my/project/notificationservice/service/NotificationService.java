package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.events.BookingNotificationEvent;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final MailSenderService mailSenderService;
	private final MessageStorageService messageStorageService;

	public void send(BookingNotificationEvent event) {
		var messageId = messageStorageService.messageId(event);

		log.info("Начата обработка уведомления, messageId={}, bookingId={}, messageType={}",
				messageId, event.bookingId(), event.messageType());

		if (!messageStorageService.save(event)) {
			log.info("Дубликат события пропущен, messageId={}, bookingId={}, messageType={}",
					messageId, event.bookingId(), event.messageType());
			return;
		}

		try {
			mailSenderService.sendMessage(event);
			messageStorageService.markStatus(messageId, MessageEntity::done);

			log.info("Уведомление успешно обработано, messageId={}, bookingId={}, messageType={}",
					messageId, event.bookingId(), event.messageType());
		} catch (MessagingException | MailException e) {
			log.error("Не удалось отправить уведомление, messageId={}, bookingId={}, messageType={}",
					messageId, event.bookingId(), event.messageType(), e);
			messageStorageService.markStatus(messageId, MessageEntity::processing);
		}
	}
}