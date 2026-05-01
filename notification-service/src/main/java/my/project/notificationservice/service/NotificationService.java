package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.events.BookingCreatedEvent;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final MailSenderService mailSenderService;
	private final MessageStorageService messageStorageService;

	public void send(BookingCreatedEvent event) {
		var messageId = event.bookingId();

		log.info("Начата обработка уведомления, bookingId={}", messageId);

		if (!messageStorageService.save(event)) {
			log.info("Дубликат события пропущен, bookingId={}", messageId);
			return;
		}

		try {
			mailSenderService.sendMessage(event);
			messageStorageService.markStatus(messageId, MessageEntity::done);

			log.info("Уведомление успешно обработано, bookingId={}", messageId);
		} catch (MessagingException | MailException e) {
			log.error("Не удалось отправить уведомление, bookingId={}", messageId, e);
			messageStorageService.markStatus(messageId, MessageEntity::processing);
		}
	}
}