package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Для кого показывать список команд: всем личным чатам или одному конкретному чату.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotCommandScope(
		String type,
		@JsonProperty("chat_id") Long chatId
) {

	public static BotCommandScope allPrivateChats() {
		return new BotCommandScope("all_private_chats", null);
	}

	public static BotCommandScope chat(long chatId) {
		return new BotCommandScope("chat", chatId);
	}
}
