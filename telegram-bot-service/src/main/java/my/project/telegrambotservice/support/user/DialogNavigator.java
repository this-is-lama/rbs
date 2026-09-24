package my.project.telegrambotservice.support.user;

import lombok.RequiredArgsConstructor;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.config.SupportProperties;
import my.project.telegrambotservice.support.dialog.DialogStep;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import org.springframework.stereotype.Component;

/**
 * Задаёт пользователю вопросы: приветствие, выбор темы и следующий шаг черновика.
 * Общая точка для команд, кнопок и ввода — чтобы «что спросить дальше» решалось в одном месте.
 */
@Component
@RequiredArgsConstructor
public class DialogNavigator {

	private final BotSender sender;
	private final SupportTicketService ticketService;
	private final SupportProperties properties;

	/** Переходит к первому незаполненному шагу черновика и задаёт его вопрос. */
	public void advance(long chatId, SupportDraft draft) {
		DialogStep step = draft.nextStep();
		DialogStep previous = draft.getStep();
		draft.setStep(step);

		switch (step) {
			case CATEGORY -> sender.html(chatId, SupportTexts.CHOOSE_CATEGORY, SupportKeyboards.categories());
			case RESTAURANT -> sender.html(chatId, SupportTexts.restaurantPrompt(draft.getCategory()), SupportKeyboards.cancelOnly());
			case DESCRIPTION -> sender.html(chatId,
					SupportTexts.descriptionPrompt(draft.getCategory(), previous == DialogStep.CATEGORY),
					SupportKeyboards.cancelOnly());
			case ATTACHMENTS -> sender.html(chatId,
					SupportTexts.attachmentsPrompt(draft.attachmentCount(), properties.maxAttachments()),
					SupportKeyboards.attachments(draft.attachmentCount()));
			case EMAIL -> sender.html(chatId, SupportTexts.emailPrompt(draft.getCategory()), SupportKeyboards.email());
			case CONFIRM -> sender.html(chatId, SupportTexts.summary(draft), SupportKeyboards.confirm());
		}
	}

	public void sendWelcome(long chatId) {
		Long activeTicketId = ticketService.findActiveTicket(chatId).map(SupportTicketEntity::getId).orElse(null);
		sender.html(chatId, SupportTexts.welcome(activeTicketId), SupportKeyboards.categories());
	}

	public void askCategory(long chatId) {
		if (rejectIfDailyLimitReached(chatId)) {
			return;
		}
		sender.html(chatId, SupportTexts.CHOOSE_CATEGORY, SupportKeyboards.categories());
	}

	/** Если лимит обращений в сутки исчерпан — сообщает об этом пользователю и возвращает {@code true}. */
	public boolean rejectIfDailyLimitReached(long chatId) {
		if (ticketService.isDailyLimitReached(chatId)) {
			sender.html(chatId, SupportTexts.dailyLimit(properties.maxTicketsPerDay()));
			return true;
		}
		return false;
	}
}
