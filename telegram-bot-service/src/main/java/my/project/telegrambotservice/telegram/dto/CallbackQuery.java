package my.project.telegrambotservice.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Нажатие inline-кнопки. {@code message} — сообщение, к которому была прикреплена кнопка.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CallbackQuery(
		String id,
		User from,
		Message message,
		String data
) {
}
