package my.project.telegrambotservice.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.config.TelegramProperties;
import my.project.telegrambotservice.telegram.client.TelegramApi;
import my.project.telegrambotservice.telegram.client.TelegramApiException;
import my.project.telegrambotservice.telegram.dto.BotCommandScope;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * При старте регистрирует меню команд: пользователям — обычный список, в чате поддержки — ещё и /tickets.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BotCommandsRegistrar {

	private final TelegramApi api;
	private final TelegramProperties properties;

	@EventListener(ApplicationReadyEvent.class)
	public void registerCommands() {
		if (!properties.enabled() || !properties.hasToken()) {
			return;
		}
		if (properties.supportChatId() == 0) {
			log.warn("TELEGRAM_SUPPORT_CHAT_ID / TELEGRAM_CHAT_ID не задан — обращениям некуда приходить");
		}

		try {
			api.setMyCommands(Commands.USER_MENU, BotCommandScope.allPrivateChats());
			if (properties.supportChatId() != 0) {
				api.setMyCommands(Commands.SUPPORT_MENU, BotCommandScope.chat(properties.supportChatId()));
			}
			log.info("Меню команд бота обновлено");
		} catch (TelegramApiException e) {
			log.warn("Не удалось обновить меню команд бота: {}", e.getMessage());
		}
	}
}
