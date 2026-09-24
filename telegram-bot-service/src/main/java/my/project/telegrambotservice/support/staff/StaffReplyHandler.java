package my.project.telegrambotservice.support.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.support.entity.MessageAuthor;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketStatus;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.client.TelegramApi;
import my.project.telegrambotservice.telegram.client.TelegramApiException;
import my.project.telegrambotservice.telegram.dto.CopyMessageRequest;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.Message.ContentType;
import my.project.telegrambotservice.telegram.dto.ReplyParameters;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Направление «поддержка → пользователь»: сотрудник отвечает (Reply) на карточку или сообщение по обращению,
 * бот доставляет ответ пользователю.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaffReplyHandler {

	private final TelegramApi api;
	private final BotSender sender;
	private final SupportTicketService ticketService;

	/**
	 * @return {@code false}, если сообщение, на которое ответили, не относится ни к одному обращению
	 */
	public boolean handleReply(Message reply) {
		long supportChatId = reply.chat().id();
		Optional<SupportTicketEntity> found = ticketService.findBySupportMessage(supportChatId, reply.replyToMessage().messageId());
		if (found.isEmpty()) {
			return false;
		}

		SupportTicketEntity ticket = found.get();
		boolean wasClosed = ticket.getStatus() == TicketStatus.CLOSED;
		ContentType type = reply.contentType();

		long deliveredMessageId;
		try {
			deliveredMessageId = deliverToUser(ticket, reply, type);
		} catch (TelegramApiException e) {
			String error = e.isBlockedByUser() ? SupportTexts.NOT_DELIVERED_BLOCKED : SupportTexts.notDelivered(e.getMessage());
			log.warn("Ответ по обращению #{} не доставлен: {}", ticket.getId(), e.getMessage());
			sender.htmlReply(supportChatId, error, reply.messageId());
			return true;
		}

		ticketService.addMessage(ticket.getId(), MessageAuthor.SUPPORT, type.name(), reply.textOrCaption(),
				deliveredMessageId, supportChatId, reply.messageId());
		log.info("Ответ поддержки доставлен по обращению #{}", ticket.getId());

		if (wasClosed) {
			sender.htmlReply(supportChatId, SupportTexts.deliveredAndReopened(ticket.getId()), reply.messageId());
		} else if (!sender.react(supportChatId, reply.messageId(), "👍")) {
			sender.htmlReply(supportChatId, SupportTexts.DELIVERED, reply.messageId());
		}
		return true;
	}

	/** Reply в групповом чате поддержки на сообщение, которое не относится к обращению. */
	public void replyTargetNotFound(Message reply) {
		sender.htmlReply(reply.chat().id(), SupportTexts.REPLY_TARGET_NOT_FOUND, reply.messageId());
	}

	private long deliverToUser(SupportTicketEntity ticket, Message reply, ContentType type) {
		long userChatId = ticket.getChatId();
		String header = SupportTexts.supportReplyHeader(ticket.getId());

		if (type == ContentType.TEXT) {
			return sender.html(userChatId, SupportTexts.supportReply(ticket.getId(), reply.text())).messageId();
		}

		if (type.supportsCaption()) {
			return api.copyMessage(CopyMessageRequest.builder()
					.chatId(userChatId)
					.fromChatId(reply.chat().id())
					.messageId(reply.messageId())
					.caption(SupportTexts.withCaption(header, reply.caption()))
					.parseMode("HTML")
					.build());
		}

		Message headerMessage = sender.html(userChatId, header);
		return api.copyMessage(CopyMessageRequest.builder()
				.chatId(userChatId)
				.fromChatId(reply.chat().id())
				.messageId(reply.messageId())
				.replyParameters(ReplyParameters.to(headerMessage.messageId()))
				.build());
	}
}
