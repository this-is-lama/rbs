package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ (Reply) на сообщение; если исходное удалено, сообщение всё равно отправится.
 */
public record ReplyParameters(
		@JsonProperty("message_id") long messageId,
		@JsonProperty("allow_sending_without_reply") boolean allowSendingWithoutReply
) {

	public static ReplyParameters to(long messageId) {
		return new ReplyParameters(messageId, true);
	}
}
