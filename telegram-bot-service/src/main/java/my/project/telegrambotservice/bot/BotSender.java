package my.project.telegrambotservice.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.telegram.client.TelegramApi;
import my.project.telegrambotservice.telegram.client.TelegramApiException;
import my.project.telegrambotservice.telegram.dto.CallbackQuery;
import my.project.telegrambotservice.telegram.dto.InlineKeyboardMarkup;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.ReplyParameters;
import my.project.telegrambotservice.telegram.dto.SendMessageRequest;
import org.springframework.stereotype.Component;

/**
 * Удобные обёртки над {@link TelegramApi} для частых действий бота.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BotSender {

	private static final String HTML = "HTML";

	private final TelegramApi api;

	public Message html(long chatId, String text) {
		return html(chatId, text, null);
	}

	public Message html(long chatId, String text, InlineKeyboardMarkup keyboard) {
		return api.sendMessage(SendMessageRequest.builder()
				.chatId(chatId)
				.text(text)
				.parseMode(HTML)
				.replyMarkup(keyboard)
				.linkPreviewOptions(SendMessageRequest.LinkPreviewOptions.disabled())
				.build());
	}

	public Message htmlReply(long chatId, String text, long replyToMessageId) {
		return api.sendMessage(SendMessageRequest.builder()
				.chatId(chatId)
				.text(text)
				.parseMode(HTML)
				.replyParameters(ReplyParameters.to(replyToMessageId))
				.linkPreviewOptions(SendMessageRequest.LinkPreviewOptions.disabled())
				.build());
	}

	/** Убирает кнопки с сообщения, чтобы их нельзя было нажать повторно. */
	public void removeKeyboard(Message message) {
		try {
			api.editMessageReplyMarkup(message.chat().id(), message.messageId(), InlineKeyboardMarkup.empty());
		} catch (TelegramApiException e) {
			// например, сообщение старше 48 часов или кнопок уже нет — не критично
			log.debug("Не удалось убрать кнопки с сообщения {}: {}", message.messageId(), e.getMessage());
		}
	}

	/** Ответ на нажатие кнопки: убирает «часики» и, если есть текст, показывает всплывающую подсказку. */
	public void answer(CallbackQuery query, String text) {
		try {
			api.answerCallbackQuery(query.id(), text);
		} catch (TelegramApiException e) {
			log.debug("Не удалось ответить на callback {}: {}", query.id(), e.getMessage());
		}
	}

	/** Реакция на сообщение — тихое «получено» без лишнего сообщения в чате. */
	public boolean react(long chatId, long messageId, String emoji) {
		try {
			api.setMessageReaction(chatId, messageId, emoji);
			return true;
		} catch (TelegramApiException e) {
			log.debug("Не удалось поставить реакцию на сообщение {}: {}", messageId, e.getMessage());
			return false;
		}
	}
}
