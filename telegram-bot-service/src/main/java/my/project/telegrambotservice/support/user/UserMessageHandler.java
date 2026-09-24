package my.project.telegrambotservice.support.user;

import lombok.RequiredArgsConstructor;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.bot.Commands;
import my.project.telegrambotservice.config.SupportProperties;
import my.project.telegrambotservice.support.dialog.DraftStorage;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.staff.SupportChatPublisher;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.dto.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Точка входа для сообщений пользователя в личном чате. Решает, что это:
 * команда, ответ на вопрос черновика, дополнение к открытому обращению или начало нового.
 */
@Component
@RequiredArgsConstructor
public class UserMessageHandler {

	/** Короче этого сообщение без команды («привет») не считаем описанием проблемы — просто показываем меню. */
	private static final int MIN_PREFILL_LENGTH = 20;

	private final UserCommandHandler commandHandler;
	private final DraftInputHandler draftInputHandler;
	private final DialogNavigator navigator;
	private final DraftStorage drafts;
	private final SupportTicketService ticketService;
	private final SupportChatPublisher publisher;
	private final BotSender sender;
	private final SupportProperties properties;

	public void handle(Message message) {
		long chatId = message.chat().id();

		String command = Commands.extract(message);
		if (command != null) {
			commandHandler.handle(chatId, command);
			return;
		}

		Optional<SupportDraft> draft = drafts.find(chatId);
		if (draft.isPresent()) {
			draftInputHandler.handle(message, draft.get());
			return;
		}

		Optional<SupportTicketEntity> activeTicket = ticketService.findActiveTicket(chatId);
		if (activeTicket.isPresent()) {
			forwardFollowUp(message, activeTicket.get());
			return;
		}

		startFromMessage(message);
	}

	/** Дописывание в открытое обращение. */
	private void forwardFollowUp(Message message, SupportTicketEntity ticket) {
		long chatId = message.chat().id();
		if (publisher.forwardFollowUp(ticket, message)) {
			// тихое «получено» вместо отдельного сообщения на каждую реплику
			sender.react(chatId, message.messageId(), "👌");
		} else {
			sender.html(chatId, SupportTexts.FOLLOW_UP_FAILED);
		}
	}

	/** Первое сообщение без команды: если оно похоже на описание проблемы, сохраняем его и спрашиваем только тему. */
	private void startFromMessage(Message message) {
		long chatId = message.chat().id();
		String text = message.textOrCaption() == null ? null : message.textOrCaption().strip();
		boolean hasAttachment = message.contentType().isAttachment();
		boolean looksLikeDescription = text != null
				&& text.length() >= MIN_PREFILL_LENGTH
				&& text.length() <= properties.maxTextLength();

		if (!looksLikeDescription && !hasAttachment) {
			navigator.sendWelcome(chatId);
			return;
		}
		if (navigator.rejectIfDailyLimitReached(chatId)) {
			return;
		}

		SupportDraft draft = drafts.create(chatId);
		if (looksLikeDescription) {
			draft.setDescription(text);
		}
		if (hasAttachment) {
			draft.addAttachment(message.messageId());
			draft.setLastMediaGroupId(message.mediaGroupId());
		}
		sender.html(chatId, SupportTexts.CHOOSE_CATEGORY_WITH_MESSAGE, SupportKeyboards.categories());
	}
}
