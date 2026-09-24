package my.project.telegrambotservice.support.view;

import my.project.telegrambotservice.support.dialog.DialogStep;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketCategory;
import my.project.telegrambotservice.support.entity.TicketRating;
import my.project.telegrambotservice.telegram.dto.InlineKeyboardButton;
import my.project.telegrambotservice.telegram.dto.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static my.project.telegrambotservice.telegram.dto.InlineKeyboardButton.callback;

public final class SupportKeyboards {

	private static final InlineKeyboardButton CANCEL = callback("✖️ Отменить", SupportCallbacks.CANCEL);

	private SupportKeyboards() {
	}

	public static InlineKeyboardMarkup categories() {
		return InlineKeyboardMarkup.column(Arrays.stream(TicketCategory.values())
				.map(category -> callback(category.label(), SupportCallbacks.of(SupportCallbacks.CATEGORY, category.name())))
				.toArray(InlineKeyboardButton[]::new));
	}

	public static InlineKeyboardMarkup cancelOnly() {
		return InlineKeyboardMarkup.column(CANCEL);
	}

	/** Пока файлов нет — «Пропустить», когда уже есть — «Готово». */
	public static InlineKeyboardMarkup attachments(int attachmentCount) {
		String skip = SupportCallbacks.of(SupportCallbacks.SKIP, DialogStep.ATTACHMENTS.name());
		InlineKeyboardButton next = attachmentCount == 0
				? callback("➡️ Пропустить", skip)
				: callback("✅ Готово", skip);
		return InlineKeyboardMarkup.column(next, CANCEL);
	}

	public static InlineKeyboardMarkup email() {
		return InlineKeyboardMarkup.column(
				callback("➡️ Пропустить", SupportCallbacks.of(SupportCallbacks.SKIP, DialogStep.EMAIL.name())),
				CANCEL
		);
	}

	public static InlineKeyboardMarkup confirm() {
		return InlineKeyboardMarkup.column(
				callback("✅ Отправить", SupportCallbacks.SEND),
				callback("✏️ Изменить описание", SupportCallbacks.EDIT),
				CANCEL
		);
	}

	public static InlineKeyboardMarkup newTicket() {
		return InlineKeyboardMarkup.column(callback("📝 Новое обращение", SupportCallbacks.NEW));
	}

	public static InlineKeyboardMarkup rating(long ticketId) {
		String id = String.valueOf(ticketId);
		return InlineKeyboardMarkup.row(
				callback("👍 Да", SupportCallbacks.of(SupportCallbacks.RATE, id, TicketRating.HELPFUL.name())),
				callback("👎 Нет", SupportCallbacks.of(SupportCallbacks.RATE, id, TicketRating.NOT_HELPFUL.name()))
		);
	}

	public static InlineKeyboardMarkup ticketActions(long ticketId) {
		return InlineKeyboardMarkup.column(callback("✅ Закрыть обращение", SupportCallbacks.support(SupportCallbacks.CLOSE, ticketId)));
	}

	/** Кнопки с номерами обращений по 4 в строке: нажатие присылает карточку, на которую можно ответить. */
	public static InlineKeyboardMarkup ticketList(List<SupportTicketEntity> tickets) {
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();
		List<InlineKeyboardButton> row = new ArrayList<>();
		for (SupportTicketEntity ticket : tickets) {
			row.add(callback("#" + ticket.getId(), SupportCallbacks.support(SupportCallbacks.SHOW, ticket.getId())));
			if (row.size() == 4) {
				rows.add(row);
				row = new ArrayList<>();
			}
		}
		if (!row.isEmpty()) {
			rows.add(row);
		}
		return new InlineKeyboardMarkup(rows);
	}
}
