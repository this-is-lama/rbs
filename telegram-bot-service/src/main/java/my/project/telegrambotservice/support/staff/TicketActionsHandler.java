package my.project.telegrambotservice.support.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.config.TelegramProperties;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.service.SupportTicketService.CloseResult;
import my.project.telegrambotservice.support.view.SupportCallbacks;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.client.TelegramApiException;
import my.project.telegrambotservice.telegram.dto.CallbackQuery;
import my.project.telegrambotservice.telegram.dto.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static my.project.telegrambotservice.support.entity.SupportMessageEntity.CARD;

/**
 * Действия сотрудника над обращениями: список открытых (/tickets), повторный показ карточки, закрытие.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketActionsHandler {

	private static final int ACTIVE_TICKETS_LIMIT = 20;

	private final BotSender sender;
	private final SupportTicketService ticketService;
	private final TelegramProperties properties;

	public void showActiveTickets() {
		long supportChatId = properties.supportChatId();
		List<SupportTicketEntity> tickets = ticketService.findActiveTickets(ACTIVE_TICKETS_LIMIT);
		if (tickets.isEmpty()) {
			sender.html(supportChatId, SupportTexts.NO_ACTIVE_TICKETS);
			return;
		}
		sender.html(supportChatId, SupportTexts.activeTickets(tickets, Instant.now()), SupportKeyboards.ticketList(tickets));
	}

	/** Кнопки чата поддержки: adm:close:12, adm:show:12. */
	public void handleCallback(CallbackQuery query) {
		String[] parts = query.data().split(":");
		Long ticketId = parts.length == 3 ? parseId(parts[2]) : null;
		if (ticketId == null) {
			sender.answer(query, SupportTexts.STALE_BUTTON);
			return;
		}

		switch (parts[1]) {
			case SupportCallbacks.CLOSE -> closeTicket(query, ticketId);
			case SupportCallbacks.SHOW -> showTicket(query, ticketId);
			default -> sender.answer(query, SupportTexts.STALE_BUTTON);
		}
	}

	private void closeTicket(CallbackQuery query, long ticketId) {
		CloseResult result = ticketService.close(ticketId);
		switch (result.outcome()) {
			case NOT_FOUND -> sender.answer(query, "Обращение #" + ticketId + " не найдено");
			case ALREADY_CLOSED -> {
				sender.answer(query, "Обращение #" + ticketId + " уже закрыто");
				sender.removeKeyboard(query.message());
			}
			case CLOSED -> {
				sender.answer(query, "Обращение #" + ticketId + " закрыто");
				sender.removeKeyboard(query.message());
				boolean userNotified = notifyUserClosed(result.ticket());
				sender.htmlReply(query.message().chat().id(),
						SupportTexts.closedBySupport(ticketId, userNotified), query.message().messageId());
			}
		}
	}

	private boolean notifyUserClosed(SupportTicketEntity ticket) {
		try {
			sender.html(ticket.getChatId(), SupportTexts.ticketClosed(ticket.getId()), SupportKeyboards.rating(ticket.getId()));
			return true;
		} catch (TelegramApiException e) {
			log.warn("Не удалось уведомить пользователя о закрытии обращения #{}: {}", ticket.getId(), e.getMessage());
			return false;
		}
	}

	/** Повторно присылает карточку — на неё можно ответить (Reply), даже если исходная карточка далеко в истории. */
	private void showTicket(CallbackQuery query, long ticketId) {
		Optional<SupportTicketEntity> found = ticketService.findById(ticketId);
		if (found.isEmpty()) {
			sender.answer(query, "Обращение #" + ticketId + " не найдено");
			return;
		}

		SupportTicketEntity ticket = found.get();
		long supportChatId = query.message().chat().id();
		sender.answer(query, null);
		Message card = sender.html(supportChatId,
				SupportTexts.ticketCard(ticket, null, false),
				ticket.getStatus().isActive() ? SupportKeyboards.ticketActions(ticketId) : null);
		ticketService.linkSupportChatMessage(ticketId, CARD, supportChatId, card.messageId());
	}

	private static Long parseId(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
