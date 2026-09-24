package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InlineKeyboardButton(
		String text,
		@JsonProperty("callback_data") String callbackData,
		String url
) {

	public static InlineKeyboardButton callback(String text, String data) {
		return new InlineKeyboardButton(text, data, null);
	}

	public static InlineKeyboardButton link(String text, String url) {
		return new InlineKeyboardButton(text, null, url);
	}
}
