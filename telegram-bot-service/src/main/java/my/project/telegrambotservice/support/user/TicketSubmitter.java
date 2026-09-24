package my.project.telegrambotservice.support.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.support.dialog.DraftStorage;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.service.NewTicket;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.staff.SupportChatPublisher;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.dto.User;
import org.springframework.stereotype.Component;

/**
 * Превращает заполненный черновик в обращение: сохраняет в БД, отправляет карточку в чат поддержки
 * и подтверждает пользователю номер обращения.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketSubmitter {

	private final SupportTicketService ticketService;
	private final SupportChatPublisher publisher;
	private final DialogNavigator navigator;
	private final DraftStorage drafts;
	private final BotSender sender;

	public void submit(long chatId, User user, SupportDraft draft) {
		if (navigator.rejectIfDailyLimitReached(chatId)) {
			return;
		}

		SupportTicketEntity ticket;
		try {
			ticket = ticketService.create(toNewTicket(chatId, user, draft));
		} catch (RuntimeException e) {
			log.error("Не удалось сохранить обращение пользователя chatId={}", chatId, e);
			// возвращаем черновик, чтобы можно было нажать «Отправить» ещё раз
			drafts.put(chatId, draft);
			sender.html(chatId, SupportTexts.SOMETHING_WENT_WRONG, SupportKeyboards.confirm());
			return;
		}

		publisher.publishNewTicket(ticket, draft.getAttachmentMessageIds());
		sender.html(chatId, SupportTexts.submitted(ticket.getId()));
	}

	private static NewTicket toNewTicket(long chatId, User user, SupportDraft draft) {
		return new NewTicket(
				chatId,
				user.displayName(),
				user.username(),
				draft.getCategory(),
				draft.getDescription(),
				draft.getRestaurant(),
				draft.getEmail()
		);
	}
}
