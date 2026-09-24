package my.project.telegrambotservice.telegram.polling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.config.TelegramProperties;
import my.project.telegrambotservice.telegram.client.TelegramApi;
import my.project.telegrambotservice.telegram.client.TelegramApiException;
import my.project.telegrambotservice.telegram.dto.Update;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Long polling: в отдельном потоке спрашивает у Telegram новые сообщения (getUpdates) и передаёт их в {@link UpdateHandler}.
 * Боту не нужен ни публичный адрес, ни открытый порт. Обновления обрабатываются по одному, поэтому
 * гонок между нажатиями одного пользователя нет.
 * <p>
 * Один токен может слушать только один экземпляр сервиса — при двух репликах Telegram отвечает 409 Conflict.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePoller implements SmartLifecycle {

	private static final long MIN_BACKOFF_MS = 1_000;
	private static final long MAX_BACKOFF_MS = 60_000;

	private final TelegramApi telegramApi;
	private final UpdateHandler updateHandler;
	private final TelegramProperties properties;

	private volatile boolean running;
	private Thread thread;
	private long offset;

	@Override
	public void start() {
		if (!properties.enabled()) {
			log.warn("Опрос Telegram выключен (app.telegram.enabled=false)");
			return;
		}
		if (!properties.hasToken()) {
			log.warn("TELEGRAM_BOT_TOKEN не задан — бот не запущен");
			return;
		}
		running = true;
		thread = Thread.ofPlatform().name("telegram-poller").start(this::pollLoop);
		log.info("Бот запущен, опрос Telegram начат, чат поддержки: {}", properties.supportChatId());
	}

	@Override
	public void stop() {
		running = false;
		if (thread != null) {
			thread.interrupt();
		}
		log.info("Опрос Telegram остановлен");
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	private void pollLoop() {
		long backoffMs = MIN_BACKOFF_MS;
		int timeoutSeconds = (int) properties.pollTimeout().toSeconds();

		while (running) {
			List<Update> updates;
			try {
				updates = telegramApi.getUpdates(offset, timeoutSeconds);
				backoffMs = MIN_BACKOFF_MS;
			} catch (TelegramApiException e) {
				if (!running) {
					return;
				}
				if (e.getErrorCode() == 409) {
					log.error("Этот токен уже слушает другой экземпляр бота (409 Conflict). "
							+ "Остановите второй экземпляр или задайте TELEGRAM_BOT_ENABLED=false");
				} else {
					log.warn("Не удалось получить обновления: {}. Повтор через {} мс", e.getMessage(), backoffMs);
				}
				sleep(backoffMs);
				backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
				continue;
			} catch (RuntimeException e) {
				if (!running) {
					return;
				}
				log.error("Неожиданная ошибка при получении обновлений. Повтор через {} мс", backoffMs, e);
				sleep(backoffMs);
				backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
				continue;
			}

			for (Update update : updates) {
				// сдвигаем offset до обработки: сообщение, на котором бот падает, не должно приходить по кругу
				offset = update.updateId() + 1;
				try {
					updateHandler.handle(update);
				} catch (Exception e) {
					log.error("Ошибка обработки обновления update_id={}", update.updateId(), e);
				}
			}
		}
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			running = false;
		}
	}
}
