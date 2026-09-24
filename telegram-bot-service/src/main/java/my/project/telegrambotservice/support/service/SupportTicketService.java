package my.project.telegrambotservice.support.service;

import my.project.telegrambotservice.support.entity.MessageAuthor;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketRating;

import java.util.List;
import java.util.Optional;

public interface SupportTicketService {

	/** Пользователь уже создал максимум обращений за последние сутки. */
	boolean isDailyLimitReached(long chatId);

	/** Последнее незакрытое обращение пользователя. */
	Optional<SupportTicketEntity> findActiveTicket(long chatId);

	Optional<SupportTicketEntity> findById(long ticketId);

	SupportTicketEntity create(NewTicket ticket);

	/**
	 * Сохраняет сообщение переписки и двигает статус: сообщение пользователя — {@code OPEN}, ответ поддержки — {@code ANSWERED}.
	 */
	void addMessage(long ticketId, MessageAuthor author, String contentType, String text,
	                Long userMessageId, Long supportChatId, Long supportMessageId);

	/**
	 * Запоминает служебное сообщение бота в чате поддержки (карточку обращения), чтобы ответ (Reply) на него
	 * тоже находил обращение. Статус обращения не меняется.
	 */
	void linkSupportChatMessage(long ticketId, String contentType, long supportChatId, long supportMessageId);

	/** Обращение, к которому относится сообщение в чате поддержки (карточка, копия вложения, ответ). */
	Optional<SupportTicketEntity> findBySupportMessage(long supportChatId, long supportMessageId);

	CloseResult close(long ticketId);

	/** Оценка ставится один раз и только владельцем закрытого обращения. */
	boolean rate(long ticketId, long chatId, TicketRating rating);

	/** Незакрытые обращения, самые давние — первыми. */
	List<SupportTicketEntity> findActiveTickets(int limit);

	record CloseResult(Outcome outcome, SupportTicketEntity ticket) {

		public enum Outcome {
			CLOSED,
			ALREADY_CLOSED,
			NOT_FOUND
		}
	}
}
