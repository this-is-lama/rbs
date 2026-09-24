package my.project.telegrambotservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Подключение к Telegram Bot API.
 *
 * @param enabled       опрашивать ли Telegram; один токен может слушать только один экземпляр
 * @param token         токен бота от @BotFather
 * @param supportChatId чат, куда бот присылает обращения и где на них отвечают
 * @param apiUrl        адрес Bot API
 * @param pollTimeout   сколько Telegram держит запрос getUpdates, если новых сообщений нет
 */
@ConfigurationProperties(prefix = "app.telegram")
public record TelegramProperties(
		boolean enabled,
		String token,
		long supportChatId,
		String apiUrl,
		Duration pollTimeout
) {

	public boolean hasToken() {
		return token != null && !token.isBlank() && !"changeme".equals(token);
	}
}
