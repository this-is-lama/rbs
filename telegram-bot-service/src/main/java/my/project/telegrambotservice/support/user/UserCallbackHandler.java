package my.project.telegrambotservice.support.user;

import lombok.RequiredArgsConstructor;
import my.project.telegrambotservice.bot.BotSender;
import my.project.telegrambotservice.support.dialog.DialogStep;
import my.project.telegrambotservice.support.dialog.DraftStorage;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.entity.TicketCategory;
import my.project.telegrambotservice.support.entity.TicketRating;
import my.project.telegrambotservice.support.service.SupportTicketService;
import my.project.telegrambotservice.support.staff.SupportChatPublisher;
import my.project.telegrambotservice.support.view.SupportCallbacks;
import my.project.telegrambotservice.support.view.SupportKeyboards;
import my.project.telegrambotservice.support.view.SupportTexts;
import my.project.telegrambotservice.telegram.dto.CallbackQuery;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Кнопки в личном чате: выбор темы, «Пропустить»/«Готово», «Отправить», «Изменить описание», «Отменить», оценка.
 * Нажатая кнопка убирается с сообщения, а устаревшие нажатия игнорируются с подсказкой.
 */
@Component
@RequiredArgsConstructor
public class UserCallbackHandler {

	private final DraftStorage drafts;
	private final DialogNavigator navigator;
	private final TicketSubmitter submitter;
	private final SupportTicketService ticketService;
	private final SupportChatPublisher publisher;
	private final BotSender sender;

	public void handle(CallbackQuery query) {
		String[] parts = query.data() == null ? new String[]{""} : query.data().split(":");
		switch (parts[0]) {
			case SupportCallbacks.CATEGORY -> onCategory(query, parts);
			case SupportCallbacks.SKIP -> onSkip(query, parts);
			case SupportCallbacks.SEND -> onSend(query);
			case SupportCallbacks.EDIT -> onEdit(query);
			case SupportCallbacks.CANCEL -> onCancel(query);
			case SupportCallbacks.NEW -> onNewTicket(query);
			case SupportCallbacks.RATE -> onRate(query, parts);
			default -> staleButton(query);
		}
	}

	private void onCategory(CallbackQuery query, String[] parts) {
		long chatId = chatId(query);
		TicketCategory category = parseEnum(TicketCategory.class, parts, 1);
		SupportDraft draft = drafts.find(chatId).orElse(null);
		if (category == null || (draft != null && draft.getCategory() != null)) {
			staleButton(query);
			return;
		}

		accept(query);
		if (draft == null) {
			if (navigator.rejectIfDailyLimitReached(chatId)) {
				return;
			}
			draft = drafts.create(chatId);
		}
		draft.setCategory(category);
		navigator.advance(chatId, draft);
	}

	/** «Пропустить» или «Готово» на необязательном шаге (скриншоты, email). */
	private void onSkip(CallbackQuery query, String[] parts) {
		DialogStep step = parseEnum(DialogStep.class, parts, 1);
		Optional<SupportDraft> draft = currentDraftAt(query, step);
		if (step == null || draft.isEmpty()) {
			staleButton(query);
			return;
		}

		if (step == DialogStep.ATTACHMENTS) {
			draft.get().setAttachmentsDone(true);
		} else if (step == DialogStep.EMAIL) {
			draft.get().setEmailDone(true);
		}
		accept(query);
		navigator.advance(chatId(query), draft.get());
	}

	private void onEdit(CallbackQuery query) {
		Optional<SupportDraft> draft = currentDraftAt(query, DialogStep.CONFIRM);
		if (draft.isEmpty()) {
			staleButton(query);
			return;
		}

		draft.get().setDescription(null);
		draft.get().setStep(DialogStep.DESCRIPTION);
		accept(query);
		sender.html(chatId(query), SupportTexts.DESCRIPTION_AGAIN, SupportKeyboards.cancelOnly());
	}

	private void onCancel(CallbackQuery query) {
		long chatId = chatId(query);
		accept(query);
		if (drafts.remove(chatId).isPresent()) {
			sender.html(chatId, SupportTexts.CANCELLED);
		}
	}

	private void onNewTicket(CallbackQuery query) {
		long chatId = chatId(query);
		accept(query);
		drafts.remove(chatId);
		navigator.askCategory(chatId);
	}

	private void onSend(CallbackQuery query) {
		long chatId = chatId(query);
		if (currentDraftAt(query, DialogStep.CONFIRM).isEmpty()) {
			staleButton(query);
			return;
		}

		// удаляем черновик до отправки: повторное нажатие «Отправить» уже ничего не найдёт
		SupportDraft draft = drafts.remove(chatId).orElseThrow();
		sender.answer(query, "Отправляю…");
		sender.removeKeyboard(query.message());
		submitter.submit(chatId, query.from(), draft);
	}

	private void onRate(CallbackQuery query, String[] parts) {
		long chatId = chatId(query);
		Long ticketId = parseLong(parts, 1);
		TicketRating rating = parseEnum(TicketRating.class, parts, 2);
		if (ticketId == null || rating == null || !ticketService.rate(ticketId, chatId, rating)) {
			staleButton(query);
			return;
		}

		accept(query);
		sender.html(chatId, SupportTexts.rated(rating),
				rating == TicketRating.NOT_HELPFUL ? SupportKeyboards.newTicket() : null);
		publisher.publishRating(ticketId, rating);
	}

	// ---------- Вспомогательное ----------

	/** Черновик пользователя, если он сейчас на шаге {@code step}. */
	private Optional<SupportDraft> currentDraftAt(CallbackQuery query, DialogStep step) {
		return drafts.find(chatId(query)).filter(draft -> step != null && draft.getStep() == step);
	}

	/** Нажатие принято: убираем «часики» и кнопки с сообщения. */
	private void accept(CallbackQuery query) {
		sender.answer(query, null);
		sender.removeKeyboard(query.message());
	}

	private void staleButton(CallbackQuery query) {
		sender.answer(query, SupportTexts.STALE_BUTTON);
		sender.removeKeyboard(query.message());
	}

	private static long chatId(CallbackQuery query) {
		return query.message().chat().id();
	}

	private static <E extends Enum<E>> E parseEnum(Class<E> type, String[] parts, int index) {
		if (parts.length <= index) {
			return null;
		}
		try {
			return Enum.valueOf(type, parts[index]);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static Long parseLong(String[] parts, int index) {
		if (parts.length <= index) {
			return null;
		}
		try {
			return Long.parseLong(parts[index]);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
