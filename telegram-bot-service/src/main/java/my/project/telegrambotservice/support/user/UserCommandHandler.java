package my.project.telegrambotservice.support.user;

import lombok.RequiredArgsConstructor;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.bot.Commands;
import my.project.telegrambotservice.config.SupportProperties;
import my.project.telegrambotservice.support.dialog.DraftStorage;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.entity.TicketCategory;
import my.project.telegrambotservice.support.view.SupportTexts;
import org.springframework.stereotype.Component;

/**
 * Команды пользователя: /start, /help, /feedback, /restaurant, /cancel.
 */
@Component
@RequiredArgsConstructor
public class UserCommandHandler {

	private final DraftStorage drafts;
	private final DialogNavigator navigator;
	private final BotSender sender;
	private final SupportProperties properties;

	public void handle(long chatId, String command) {
		switch (command) {
			case Commands.START -> {
				drafts.remove(chatId);
				navigator.sendWelcome(chatId);
			}
			case Commands.HELP -> sender.html(chatId, SupportTexts.help(properties.siteUrl()));
			case Commands.FEEDBACK, Commands.NEW -> {
				drafts.remove(chatId);
				navigator.askCategory(chatId);
			}
			case Commands.RESTAURANT -> startRestaurantTicket(chatId);
			case Commands.CANCEL -> sender.html(chatId,
					drafts.remove(chatId).isPresent() ? SupportTexts.CANCELLED : SupportTexts.NOTHING_TO_CANCEL);
			default -> sender.html(chatId, SupportTexts.UNKNOWN_COMMAND);
		}
	}

	private void startRestaurantTicket(long chatId) {
		drafts.remove(chatId);
		if (navigator.rejectIfDailyLimitReached(chatId)) {
			return;
		}
		SupportDraft draft = drafts.create(chatId);
		draft.setCategory(TicketCategory.RESTAURANT);
		navigator.advance(chatId, draft);
	}
}
