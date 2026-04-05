package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.mapper.MessageJsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRetryService {

	private static final Duration timeAfterDone = Duration.ofHours(2);

	@Value("${app.notification.max-attempts:5}")
	private int maxAttempts;

	@Value("${app.notification.stuck-minutes:15}")
	private int stuckMinutes;

	private final MessageStorageService messageStorageService;
	private final MailSenderService mailSenderService;
	private final MessageJsonMapper mapper;

	@Scheduled(fixedDelayString = "PT10M")
	public void retry() {
		log.info("Запущена повторная обработка уведомлений");

		var failed = messageStorageService.getWorkBatch(maxAttempts, stuckMinutes);

		for (var message : failed) {
			var event = mapper.readJson(message.getJsonMessage());

			try {
				mailSenderService.sendMessage(event);
				messageStorageService.markStatus(event.bookingId(), MessageEntity::done);

				log.info("Повторная отправка уведомления выполнена успешно, bookingId={}", event.bookingId());
			} catch (MessagingException e) {
				if (message.getAttempts() >= maxAttempts) {
					messageStorageService.markStatus(event.bookingId(), MessageEntity::fail);
				}

				log.error("Повторная отправка уведомления завершилась ошибкой, bookingId={}, attempts={}/{}",
						event.bookingId(), message.getAttempts() + 1, maxAttempts, e);
			}
		}

		log.info("Повторная обработка уведомлений завершена, count={}", failed.size());
	}

	@Scheduled(fixedDelayString = "PT60M")
	public void clean() {
		Instant time = Instant.now().minus(timeAfterDone);
		long deleted = messageStorageService.cleanDone(time);

		log.info("Очистка DONE-сообщений завершена, deleted={}, olderThan={}", deleted, time);
	}
}