package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMessageRequest(
		@JsonProperty("chat_id") long chatId,
		String text,
		@JsonProperty("parse_mode") String parseMode,
		@JsonProperty("reply_markup") InlineKeyboardMarkup replyMarkup,
		@JsonProperty("reply_parameters") ReplyParameters replyParameters,
		@JsonProperty("link_preview_options") LinkPreviewOptions linkPreviewOptions
) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record LinkPreviewOptions(@JsonProperty("is_disabled") Boolean isDisabled) {

		public static LinkPreviewOptions disabled() {
			return new LinkPreviewOptions(true);
		}
	}
}
