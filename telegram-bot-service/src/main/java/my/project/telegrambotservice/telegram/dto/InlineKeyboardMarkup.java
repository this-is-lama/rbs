package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InlineKeyboardMarkup(
		@JsonProperty("inline_keyboard") List<List<InlineKeyboardButton>> inlineKeyboard
) {

	/** Клавиатура, где каждая кнопка на отдельной строке. */
	public static InlineKeyboardMarkup column(InlineKeyboardButton... buttons) {
		return new InlineKeyboardMarkup(java.util.Arrays.stream(buttons).map(List::of).toList());
	}

	/** Все кнопки в одну строку. */
	public static InlineKeyboardMarkup row(InlineKeyboardButton... buttons) {
		return new InlineKeyboardMarkup(List.of(List.of(buttons)));
	}

	/** Пустая клавиатура — так кнопки убирают с уже отправленного сообщения. */
	public static InlineKeyboardMarkup empty() {
		return new InlineKeyboardMarkup(List.of());
	}
}
