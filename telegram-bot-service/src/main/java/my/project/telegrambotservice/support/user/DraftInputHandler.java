package my.project.telegrambotservice.support.user;

import lombok.RequiredArgsConstructor;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.config.SupportProperties;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.Message.ContentType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Ответы пользователя на вопросы черновика: проверяет ввод для текущего шага, сохраняет его и переходит дальше.
 */
@Component
@RequiredArgsConstructor
public class DraftInputHandler {

	private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
	private static final int MAX_RESTAURANT_LENGTH = 500;
	private static final int MAX_EMAIL_LENGTH = 255;

	private final DialogNavigator navigator;
	private final BotSender sender;
	private final SupportProperties properties;

	public void handle(Message message, SupportDraft draft) {
		long chatId = message.chat().id();

		// остальные фото альбома приходят отдельными сообщениями — молча добавляем их к первому
		if (isSameMediaGroup(message, draft)) {
			if (message.contentType().isAttachment() && draft.attachmentCount() < properties.maxAttachments()) {
				draft.addAttachment(message.messageId());
			}
			return;
		}

		switch (draft.getStep()) {
			case CATEGORY -> sender.html(chatId, SupportTexts.CHOOSE_CATEGORY_HINT, SupportKeyboards.categories());
			case RESTAURANT -> handleRestaurant(message, draft);
			case DESCRIPTION -> handleDescription(message, draft);
			case ATTACHMENTS -> handleAttachment(message, draft);
			case EMAIL -> handleEmail(message, draft);
			case CONFIRM -> sender.html(chatId, SupportTexts.CONFIRM_HINT, SupportKeyboards.confirm());
		}
	}

	private void handleRestaurant(Message message, SupportDraft draft) {
		long chatId = message.chat().id();
		if (message.contentType() != ContentType.TEXT) {
			sender.html(chatId, SupportTexts.RESTAURANT_NEED_TEXT, SupportKeyboards.cancelOnly());
			return;
		}

		String text = message.text().strip();
		if (!isValidLength(chatId, text, MAX_RESTAURANT_LENGTH)) {
			return;
		}
		draft.setRestaurant(text);
		navigator.advance(chatId, draft);
	}

	private void handleDescription(Message message, SupportDraft draft) {
		long chatId = message.chat().id();
		ContentType type = message.contentType();

		if (type.isAttachment()) {
			draft.setLastMediaGroupId(message.mediaGroupId());
			if (draft.attachmentCount() < properties.maxAttachments()) {
				draft.addAttachment(message.messageId());
			}
			String caption = message.caption() == null ? null : message.caption().strip();
			if (caption == null || caption.isEmpty()) {
				sender.html(chatId, SupportTexts.DESCRIPTION_MEDIA_WITHOUT_TEXT, SupportKeyboards.cancelOnly());
				return;
			}
			if (!isValidLength(chatId, caption, properties.maxTextLength())) {
				return;
			}
			draft.setDescription(caption);
			navigator.advance(chatId, draft);
			return;
		}

		if (type != ContentType.TEXT) {
			sender.html(chatId, SupportTexts.DESCRIPTION_NEED_TEXT, SupportKeyboards.cancelOnly());
			return;
		}

		String text = message.text().strip();
		if (!isValidLength(chatId, text, properties.maxTextLength())) {
			return;
		}
		draft.setDescription(text);
		navigator.advance(chatId, draft);
	}

	private void handleAttachment(Message message, SupportDraft draft) {
		long chatId = message.chat().id();
		ContentType type = message.contentType();

		if (type.isAttachment()) {
			draft.setLastMediaGroupId(message.mediaGroupId());
			if (draft.attachmentCount() >= properties.maxAttachments()) {
				sender.html(chatId, SupportTexts.attachmentLimit(properties.maxAttachments()),
						SupportKeyboards.attachments(draft.attachmentCount()));
				return;
			}
			draft.addAttachment(message.messageId());
			if (message.caption() != null && !message.caption().isBlank()) {
				appendToDescription(draft, message.caption().strip());
			}
			sender.html(chatId, SupportTexts.ATTACHMENT_ADDED, SupportKeyboards.attachments(draft.attachmentCount()));
			return;
		}

		if (type == ContentType.TEXT) {
			String combined = draft.getDescription() + "\n\n" + message.text().strip();
			if (combined.length() > properties.maxTextLength()) {
				sender.html(chatId, SupportTexts.textTooLong(combined.length(), properties.maxTextLength()),
						SupportKeyboards.attachments(draft.attachmentCount()));
				return;
			}
			draft.setDescription(combined);
			sender.html(chatId, SupportTexts.TEXT_ADDED_TO_DESCRIPTION, SupportKeyboards.attachments(draft.attachmentCount()));
			return;
		}

		sender.html(chatId, SupportTexts.ATTACHMENT_WRONG_TYPE, SupportKeyboards.attachments(draft.attachmentCount()));
	}

	private void handleEmail(Message message, SupportDraft draft) {
		long chatId = message.chat().id();
		String email = message.text() == null ? "" : message.text().strip();
		if (email.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(email).matches()) {
			sender.html(chatId, SupportTexts.EMAIL_INVALID, SupportKeyboards.email());
			return;
		}
		draft.setEmail(email);
		draft.setEmailDone(true);
		navigator.advance(chatId, draft);
	}

	private boolean isValidLength(long chatId, String text, int maxLength) {
		if (text.length() < properties.minTextLength()) {
			sender.html(chatId, SupportTexts.textTooShort(properties.minTextLength()), SupportKeyboards.cancelOnly());
			return false;
		}
		if (text.length() > maxLength) {
			sender.html(chatId, SupportTexts.textTooLong(text.length(), maxLength), SupportKeyboards.cancelOnly());
			return false;
		}
		return true;
	}

	private void appendToDescription(SupportDraft draft, String text) {
		String combined = draft.getDescription() + "\n\n" + text;
		if (combined.length() <= properties.maxTextLength()) {
			draft.setDescription(combined);
		}
	}

	private static boolean isSameMediaGroup(Message message, SupportDraft draft) {
		return message.mediaGroupId() != null && message.mediaGroupId().equals(draft.getLastMediaGroupId());
	}
}
