package my.project.telegrambotservice.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.config.TelegramProperties;
import my.project.telegrambotservice.support.staff.StaffReplyHandler;
import my.project.telegrambotservice.support.staff.TicketActionsHandler;
import my.project.telegrambotservice.support.user.UserCallbackHandler;
import my.project.telegrambotservice.support.user.UserMessageHandler;
import my.project.telegrambotservice.support.view.SupportCallbacks;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.dto.CallbackQuery;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.Update;
import my.project.telegrambotservice.telegram.polling.UpdateHandler;
import org.springframework.stereotype.Component;

/**
 * Единая точка входа для всех обновлений: решает, кто их обрабатывает — сторона пользователя
 * ({@code support.user}) или сторона поддержки ({@code support.staff}). Сам никакой логики сценария не содержит.
 * <p>
 * Чат поддержки может быть и личным чатом администратора с ботом. Тогда в нём работают обе роли:
 * Reply на карточку или сообщение по обращению — это ответ поддержки, всё остальное — обычный пользовательский сценарий
 * (удобно, чтобы проверить бота на себе).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateRouter implements UpdateHandler {

	private final TelegramProperties properties;
	private final UserMessageHandler userMessageHandler;
	private final UserCallbackHandler userCallbackHandler;
	private final StaffReplyHandler staffReplyHandler;
	private final TicketActionsHandler ticketActionsHandler;
	private final BotSender sender;

	@Override
	public void handle(Update update) {
		if (update.callbackQuery() != null) {
			routeCallback(update.callbackQuery());
		} else if (update.message() != null) {
			routeMessage(update.message());
		}
	}

	private void routeMessage(Message message) {
		if (message.from() == null || message.from().isBot() || message.chat() == null) {
			return;
		}

		if (isSupportChat(message)) {
			if (message.replyToMessage() != null && staffReplyHandler.handleReply(message)) {
				return;
			}
			if (Commands.is(message, Commands.TICKETS)) {
				ticketActionsHandler.showActiveTickets();
				return;
			}
			if (!message.chat().isPrivate()) {
				// в групповом чате поддержки обычные сообщения сотрудников бот не трогает
				if (message.replyToMessage() != null) {
					staffReplyHandler.replyTargetNotFound(message);
				}
				return;
			}
		}

		if (message.chat().isPrivate()) {
			userMessageHandler.handle(message);
		}
	}

	private void routeCallback(CallbackQuery query) {
		Message message = query.message();
		if (message == null || message.chat() == null) {
			sender.answer(query, null);
			return;
		}

		String data = query.data() == null ? "" : query.data();
		if (data.startsWith(SupportCallbacks.SUPPORT_PREFIX)) {
			if (isSupportChat(message)) {
				ticketActionsHandler.handleCallback(query);
			} else {
				log.warn("Кнопка чата поддержки нажата вне чата поддержки, chatId={}", message.chat().id());
				sender.answer(query, SupportTexts.STALE_BUTTON);
			}
			return;
		}

		if (message.chat().isPrivate()) {
			userCallbackHandler.handle(query);
		} else {
			sender.answer(query, null);
		}
	}

	private boolean isSupportChat(Message message) {
		return message.chat().id() == properties.supportChatId();
	}
}
