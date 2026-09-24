package my.project.telegrambotservice.support.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.bot.Html;
import my.project.telegrambotservice.config.TelegramProperties;
import my.project.telegrambotservice.support.entity.MessageAuthor;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketRating;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.client.TelegramApi;
import my.project.telegrambotservice.telegram.client.TelegramApiException;
import my.project.telegrambotservice.telegram.dto.CopyMessageRequest;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.Message.ContentType;
import my.project.telegrambotservice.telegram.dto.ReplyParameters;
import org.springframework.stereotype.Component;

import java.util.List;

import static my.project.telegrambotservice.support.entity.SupportMessageEntity.ATTACHMENT;
import static my.project.telegrambotservice.support.entity.SupportMessageEntity.CARD;
import static my.project.telegrambotservice.support.entity.SupportMessageEntity.HEADER;

/**
 * Направление «пользователь → поддержка»: карточка нового обращения, сообщения по открытому обращению, оценки.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SupportChatPublisher {

	private static final int MAX_FORWARDED_TEXT = 3500;

	private final TelegramApi api;
	private final BotSender sender;
	private final SupportTicketService ticketService;
	private final TelegramProperties properties;

	/** Карточка нового обращения и копии вложений ответом на неё. */
	public void publishNewTicket(SupportTicketEntity ticket, List<Long> attachmentMessageIds) {
		long supportChatId = properties.supportChatId();
		long ticketId = ticket.getId();

		Message card;
		try {
			card = sender.html(supportChatId,
					SupportTexts.ticketCard(ticket, attachmentMessageIds.size(), true),
					SupportKeyboards.ticketActions(ticketId));
		} catch (TelegramApiException e) {
			// обращение уже сохранено в БД — его будет видно в /tickets, когда чат поддержки заработает
			log.error("Не удалось отправить карточку обращения #{} в чат поддержки {}: {}", ticketId, supportChatId, e.getMessage());
			return;
		}
		ticketService.linkSupportChatMessage(ticketId, CARD, supportChatId, card.messageId());

		for (Long attachmentId : attachmentMessageIds) {
			try {
				long copyId = api.copyMessage(CopyMessageRequest.builder()
						.chatId(supportChatId)
						.fromChatId(ticket.getChatId())
						.messageId(attachmentId)
						.caption(SupportTexts.attachmentCaption(ticketId))
						.replyParameters(ReplyParameters.to(card.messageId()))
						.build());
				ticketService.addMessage(ticketId, MessageAuthor.USER, ATTACHMENT, null, attachmentId, supportChatId, copyId);
			} catch (TelegramApiException e) {
				log.warn("Не удалось переслать вложение {} обращения #{}: {}", attachmentId, ticketId, e.getMessage());
			}
		}
	}

	/**
	 * Сообщение пользователя по уже открытому обращению.
	 *
	 * @return {@code true}, если сообщение дошло до чата поддержки
	 */
	public boolean forwardFollowUp(SupportTicketEntity ticket, Message message) {
		long supportChatId = properties.supportChatId();
		String header = SupportTexts.followUpHeader(ticket);
		ContentType type = message.contentType();

		try {
			long supportMessageId;
			if (type == ContentType.TEXT) {
				String text = header + "\n\n" + Html.escape(Html.truncate(message.text(), MAX_FORWARDED_TEXT));
				supportMessageId = sender.html(supportChatId, text).messageId();
			} else if (type.supportsCaption()) {
				supportMessageId = api.copyMessage(CopyMessageRequest.builder()
						.chatId(supportChatId)
						.fromChatId(message.chat().id())
						.messageId(message.messageId())
						.caption(SupportTexts.withCaption(header, message.caption()))
						.parseMode("HTML")
						.build());
			} else {
				// стикер, кружок и т. п. — у них нет подписи, поэтому сначала заголовок, потом копия ответом на него
				Message headerMessage = sender.html(supportChatId, header + " " + SupportTexts.FOLLOW_UP_ATTACHMENT_BELOW);
				ticketService.linkSupportChatMessage(ticket.getId(), HEADER, supportChatId, headerMessage.messageId());
				supportMessageId = api.copyMessage(CopyMessageRequest.builder()
						.chatId(supportChatId)
						.fromChatId(message.chat().id())
						.messageId(message.messageId())
						.replyParameters(ReplyParameters.to(headerMessage.messageId()))
						.build());
			}

			ticketService.addMessage(ticket.getId(), MessageAuthor.USER, type.name(), message.textOrCaption(),
					message.messageId(), supportChatId, supportMessageId);
			return true;
		} catch (TelegramApiException e) {
			log.warn("Не удалось переслать сообщение пользователя по обращению #{}: {}", ticket.getId(), e.getMessage());
			return false;
		}
	}

	/** Оценка, которую пользователь поставил закрытому обращению. */
	public void publishRating(long ticketId, TicketRating rating) {
		try {
			sender.html(properties.supportChatId(), SupportTexts.ratingReceived(ticketId, rating));
		} catch (TelegramApiException e) {
			log.warn("Не удалось отправить оценку обращения #{} в чат поддержки: {}", ticketId, e.getMessage());
		}
	}
}
