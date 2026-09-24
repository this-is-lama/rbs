package my.project.telegrambotservice.telegram.client;

import lombok.Getter;

/**
 * Ошибка вызова Bot API. {@code errorCode} — код из ответа Telegram (403, 400, 429...), 0 — сетевая ошибка.
 */
@Getter
public class TelegramApiException extends RuntimeException {

	private final String method;
	private final int errorCode;

	public TelegramApiException(String method, int errorCode, String description) {
		super("Telegram API " + method + " -> " + errorCode + ": " + description);
		this.method = method;
		this.errorCode = errorCode;
	}

	/** Пользователь заблокировал бота или удалил аккаунт — писать ему больше нельзя. */
	public boolean isBlockedByUser() {
		return errorCode == 403;
	}
}
