package my.project.telegrambotservice.support.dialog;

import lombok.Getter;
import lombok.Setter;
import my.project.telegrambotservice.support.entity.TicketCategory;

import java.util.ArrayList;
import java.util.List;

import static my.project.telegrambotservice.support.dialog.DialogStep.ATTACHMENTS;
import static my.project.telegrambotservice.support.dialog.DialogStep.CATEGORY;
import static my.project.telegrambotservice.support.dialog.DialogStep.CONFIRM;
import static my.project.telegrambotservice.support.dialog.DialogStep.DESCRIPTION;
import static my.project.telegrambotservice.support.dialog.DialogStep.EMAIL;
import static my.project.telegrambotservice.support.dialog.DialogStep.RESTAURANT;

/**
 * Незаконченное обращение — то, что пользователь уже ответил боту. Хранится в памяти ({@link DraftStorage}).
 * <p>
 * Какие шаги проходить, зависит от темы:
 * <ul>
 *     <li>Ошибка на сайте: описание → скриншоты → email → отправка</li>
 *     <li>Вопрос о бронировании: описание → email → отправка</li>
 *     <li>Идея: описание → отправка</li>
 *     <li>Вопрос от ресторана: ресторан → описание → email → отправка</li>
 * </ul>
 * Следующий шаг — первый незаполненный, поэтому если пользователь сразу написал текст без команды,
 * этот текст становится описанием и шаг описания пропускается.
 */
@Getter
@Setter
public class SupportDraft {

	private TicketCategory category;
	private DialogStep step = CATEGORY;

	private String restaurant;
	private String description;
	private String email;
	private final List<Long> attachmentMessageIds = new ArrayList<>();

	private boolean attachmentsDone;
	private boolean emailDone;

	/** Альбом из нескольких фото приходит отдельными сообщениями — отвечаем на него один раз. */
	private String lastMediaGroupId;

	public DialogStep nextStep() {
		if (category == null) {
			return CATEGORY;
		}
		if (category == TicketCategory.RESTAURANT && restaurant == null) {
			return RESTAURANT;
		}
		if (description == null) {
			return DESCRIPTION;
		}
		if (category == TicketCategory.BUG && !attachmentsDone) {
			return ATTACHMENTS;
		}
		if (needsEmail() && !emailDone) {
			return EMAIL;
		}
		return CONFIRM;
	}

	public boolean needsEmail() {
		return category != TicketCategory.IDEA;
	}

	public void addAttachment(long messageId) {
		attachmentMessageIds.add(messageId);
	}

	public int attachmentCount() {
		return attachmentMessageIds.size();
	}
}
