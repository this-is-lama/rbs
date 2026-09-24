package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Копия сообщения в другой чат без пометки «Переслано от»; подпись можно заменить.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CopyMessageRequest(
		@JsonProperty("chat_id") long chatId,
		@JsonProperty("from_chat_id") long fromChatId,
		@JsonProperty("message_id") long messageId,
		String caption,
		@JsonProperty("parse_mode") String parseMode,
		@JsonProperty("reply_parameters") ReplyParameters replyParameters,
		@JsonProperty("reply_markup") InlineKeyboardMarkup replyMarkup
) {
}
