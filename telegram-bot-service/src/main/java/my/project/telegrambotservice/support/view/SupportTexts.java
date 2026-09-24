package my.project.telegrambotservice.support.view;

import my.project.telegrambotservice.bot.Html;
import my.project.telegrambotservice.support.dialog.SupportDraft;
import my.project.telegrambotservice.support.entity.SupportTicketEntity;
import my.project.telegrambotservice.support.entity.TicketCategory;
import my.project.telegrambotservice.support.entity.TicketRating;
import my.project.telegrambotservice.support.entity.TicketStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static my.project.telegrambotservice.bot.Html.escape;

/**
 * Все тексты сценария поддержки в одном месте — чтобы править сценарий, не разбираясь в логике.
 * Разметка — HTML (https://core.telegram.org/bots/api#html-style); текст пользователя всегда через {@link Html#escape}.
 */
public final class SupportTexts {

	/** Лимит подписи в Telegram — 1024 символа, оставляем запас под заголовок. */
	private static final int MAX_CAPTION = 900;

	private SupportTexts() {
	}

	// ---------- Пользователь: начало ----------

	public static String welcome(Long activeTicketId) {
		String text = """
				👋 Здравствуйте! Это поддержка <b>RBS</b> — сервиса бронирования столиков.

				Выберите, с чем помочь. Я задам пару вопросов и передам обращение команде — ответ придёт прямо сюда.""";
		if (activeTicketId != null) {
			text += "\n\nℹ️ У вас есть открытое обращение <b>#" + activeTicketId + "</b>. "
					+ "Чтобы дополнить его, просто напишите сюда.";
		}
		return text;
	}

	public static String help(String siteUrl) {
		return """
				<b>Как работает поддержка</b>

				1. Нажмите /feedback и выберите тему.
				2. Ответьте на пару вопросов: что случилось, скриншот, email.
				3. Проверьте и отправьте — обращению присвоится номер.
				4. Ответ придёт в этот чат. Чтобы что-то дописать, просто напишите сюда.

				<b>Команды</b>
				/feedback — написать в поддержку
				/restaurant — вопрос от ресторана
				/cancel — отменить заполнение
				/help — эта справка

				Забронировать столик: %s""".formatted(siteUrl);
	}

	public static final String CHOOSE_CATEGORY = "Выберите тему обращения:";

	public static final String CHOOSE_CATEGORY_WITH_MESSAGE =
			"Передам ваше сообщение в поддержку. Уточните, пожалуйста, тему:";

	public static final String CHOOSE_CATEGORY_HINT = "Пожалуйста, выберите тему кнопкой 👇";

	public static String dailyLimit(int maxTicketsPerDay) {
		return "Вы уже отправили " + maxTicketsPerDay + " обращений за последние сутки — мы обязательно на них ответим. "
				+ "Новое обращение можно будет создать позже.";
	}

	// ---------- Пользователь: шаги ----------

	public static String restaurantPrompt(TicketCategory category) {
		return "<b>" + category.label() + "</b>\n\n"
				+ "Напишите название ресторана и его адрес: город, улица, дом.";
	}

	/**
	 * Вопрос об описании. Сразу после выбора темы — с заголовком темы, иначе (после шага «ресторан») — без него.
	 */
	public static String descriptionPrompt(TicketCategory category, boolean withHeader) {
		String prompt = switch (category) {
			case BUG -> """
					Опишите, что произошло:
					• что вы делали,
					• что ожидали увидеть,
					• что получилось на самом деле.

					Можно сразу приложить скриншот с подписью.""";
			case BOOKING -> "Напишите ваш вопрос. Если он о конкретной брони — укажите ресторан, дату и время.";
			case IDEA -> "Расскажите, чего не хватает или что можно сделать удобнее. Нам важна любая идея.";
			case RESTAURANT -> "Опишите вопрос. Если не получается подтвердить ресторан — напишите, на каком шаге возникла проблема.";
		};
		return withHeader ? "<b>" + category.label() + "</b>\n\n" + prompt : "✏️ " + prompt;
	}

	public static final String DESCRIPTION_AGAIN = "✏️ Напишите новое описание — оно заменит прежнее.";

	public static final String DESCRIPTION_NEED_TEXT = "Пожалуйста, опишите вопрос текстом.";

	public static final String DESCRIPTION_MEDIA_WITHOUT_TEXT = "Файл сохранил 👍 Теперь опишите проблему текстом.";

	public static final String RESTAURANT_NEED_TEXT = "Напишите название и адрес ресторана текстом.";

	public static String attachmentsPrompt(int attachmentCount, int maxAttachments) {
		if (attachmentCount > 0) {
			return "📎 Файл уже приложен. Можно отправить ещё (всего до " + maxAttachments + ") или нажать «Готово».";
		}
		return "📎 Если есть скриншот — отправьте его сюда, можно несколько (до " + maxAttachments + "). "
				+ "Он поможет быстрее разобраться.\n\nЕсли скриншота нет — нажмите «Пропустить».";
	}

	public static final String ATTACHMENT_ADDED = "✅ Получил. Можно отправить ещё или нажать «Готово».";

	public static String attachmentLimit(int maxAttachments) {
		return "Можно приложить не больше " + maxAttachments + " файлов. Нажмите «Готово», чтобы продолжить.";
	}

	public static final String ATTACHMENT_WRONG_TYPE =
			"Сюда можно отправить фото, видео или файл. Если скриншота нет — нажмите «Пропустить».";

	public static final String TEXT_ADDED_TO_DESCRIPTION = "Добавил текст к описанию. Отправьте скриншот или нажмите кнопку ниже.";

	public static String emailPrompt(TicketCategory category) {
		String purpose = category == TicketCategory.RESTAURANT
				? "email аккаунта менеджера ресторана на сайте — так мы быстрее найдём ваш ресторан."
				: "email, на который зарегистрирован ваш аккаунт на сайте, — так мы быстрее найдём ваши брони.";
		return "📧 Укажите " + purpose + "\n\nЕсли не хотите — нажмите «Пропустить».";
	}

	public static final String EMAIL_INVALID =
			"Похоже, в адресе опечатка. Напишите его в виде <code>name@mail.ru</code> или нажмите «Пропустить».";

	public static String textTooShort(int minLength) {
		return "Напишите чуть подробнее — хотя бы " + minLength + " символов.";
	}

	public static String textTooLong(int length, int maxLength) {
		return "Сообщение слишком длинное: " + length + " символов, а можно до " + maxLength + ". "
				+ "Сократите его: основное — сейчас, остальное можно дописать после отправки обращения.";
	}

	public static String summary(SupportDraft draft) {
		StringBuilder text = new StringBuilder("<b>Проверьте обращение</b>\n\n");
		text.append("Тема: ").append(draft.getCategory().label()).append('\n');
		if (draft.getRestaurant() != null) {
			text.append("Ресторан: ").append(escape(draft.getRestaurant())).append('\n');
		}
		if (draft.getEmail() != null) {
			text.append("Email: ").append(escape(draft.getEmail())).append('\n');
		}
		if (draft.attachmentCount() > 0) {
			text.append("Вложений: ").append(draft.attachmentCount()).append('\n');
		}
		text.append("\n<blockquote>").append(escape(draft.getDescription())).append("</blockquote>\n\n");
		text.append("Всё верно?");
		return text.toString();
	}

	public static final String CONFIRM_HINT = "Нажмите «Отправить», чтобы передать обращение, или «Изменить описание».";

	// ---------- Пользователь: после отправки ----------

	public static String submitted(long ticketId) {
		return """
				✅ Обращение <b>#%d</b> принято!

				Мы ответим прямо в этом чате, обычно в течение дня. \
				Если захотите что-то добавить — просто напишите сюда, сообщение попадёт в это же обращение.""".formatted(ticketId);
	}

	public static final String CANCELLED = "Заполнение отменено. Если понадобится помощь — /feedback";

	public static final String NOTHING_TO_CANCEL = "Сейчас нечего отменять. Чтобы написать в поддержку — /feedback";

	public static final String UNKNOWN_COMMAND = "Не знаю такой команды. Список команд — /help";

	public static final String FOLLOW_UP_FAILED = "Не получилось передать это сообщение. Попробуйте отправить текст или фото.";

	public static final String SOMETHING_WENT_WRONG = "Что-то пошло не так. Попробуйте ещё раз через пару минут.";

	public static final String STALE_BUTTON = "Эта кнопка уже неактуальна";

	public static String supportReply(long ticketId, String text) {
		return supportReplyHeader(ticketId) + "\n\n" + escape(text);
	}

	public static String supportReplyHeader(long ticketId) {
		return "💬 <b>Ответ поддержки</b> · обращение #" + ticketId;
	}

	public static String ticketClosed(long ticketId) {
		return "✅ Обращение <b>#" + ticketId + "</b> закрыто.\n\nПомогли ли мы вам?";
	}

	public static String rated(TicketRating rating) {
		return rating == TicketRating.HELPFUL
				? "Спасибо за оценку! Рады, что смогли помочь 🙌"
				: "Спасибо за честность. Если вопрос остался — напишите сюда, и мы откроем новое обращение.";
	}

	// ---------- Чат поддержки ----------

	/**
	 * Карточка обращения. {@code attachmentCount} — {@code null}, если количество вложений не известно (повторный показ).
	 */
	public static String ticketCard(SupportTicketEntity ticket, Integer attachmentCount, boolean isNew) {
		StringBuilder text = new StringBuilder();
		text.append(isNew ? "🆕 " : "📋 ")
				.append("<b>Обращение #").append(ticket.getId()).append("</b> · ")
				.append(ticket.getCategory().label()).append("\n\n");

		text.append("👤 ").append(userLink(ticket));
		if (ticket.getUsername() != null) {
			text.append(" (@").append(escape(ticket.getUsername())).append(')');
		}
		text.append('\n');
		if (ticket.getEmail() != null) {
			text.append("📧 ").append(escape(ticket.getEmail())).append('\n');
		}
		if (ticket.getRestaurant() != null) {
			text.append("🏢 ").append(escape(ticket.getRestaurant())).append('\n');
		}
		if (attachmentCount != null && attachmentCount > 0) {
			text.append("📎 Вложений: ").append(attachmentCount).append(" — ниже\n");
		}
		if (!isNew) {
			text.append("Статус: ").append(statusLabel(ticket.getStatus())).append('\n');
		}

		text.append("\n<blockquote>").append(escape(Html.truncate(ticket.getDescription(), 3000))).append("</blockquote>\n\n");
		text.append("<i>Ответьте (Reply) на это сообщение, чтобы написать пользователю.</i>");
		return text.toString();
	}

	/** Подпись к копии вложения: наш заголовок + исходная подпись, если была. */
	public static String withCaption(String header, String originalCaption) {
		if (originalCaption == null || originalCaption.isBlank()) {
			return header;
		}
		return header + "\n\n" + escape(Html.truncate(originalCaption, MAX_CAPTION));
	}

	public static String attachmentCaption(long ticketId) {
		return "📎 Вложение к обращению #" + ticketId;
	}

	public static String followUpHeader(SupportTicketEntity ticket) {
		return "💬 <b>#" + ticket.getId() + "</b> · " + escape(ticket.getUserDisplayName()) + ":";
	}

	public static final String FOLLOW_UP_ATTACHMENT_BELOW = "<i>вложение ниже</i>";

	public static final String DELIVERED = "✅ Отправлено пользователю.";

	public static String deliveredAndReopened(long ticketId) {
		return "✅ Отправлено. Обращение #" + ticketId + " было закрыто — снова открыто.";
	}

	public static final String NOT_DELIVERED_BLOCKED = "⚠️ Не доставлено: пользователь заблокировал бота.";

	public static String notDelivered(String reason) {
		return "⚠️ Не удалось доставить сообщение: " + escape(reason);
	}

	public static final String REPLY_TARGET_NOT_FOUND =
			"Не нашёл обращение для этого сообщения. Ответьте (Reply) на карточку обращения или на сообщение пользователя.";

	public static String closedBySupport(long ticketId, boolean userNotified) {
		return userNotified
				? "✅ Обращение #" + ticketId + " закрыто, пользователь получил уведомление."
				: "✅ Обращение #" + ticketId + " закрыто. Уведомить пользователя не получилось — возможно, он заблокировал бота.";
	}

	public static String ratingReceived(long ticketId, TicketRating rating) {
		return "⭐ Оценка по обращению #" + ticketId + ": "
				+ (rating == TicketRating.HELPFUL ? "👍 помогли" : "👎 не помогли");
	}

	public static final String NO_ACTIVE_TICKETS = "🎉 Открытых обращений нет.";

	public static String activeTickets(List<SupportTicketEntity> tickets, Instant now) {
		StringBuilder text = new StringBuilder("<b>Открытые обращения: ").append(tickets.size()).append("</b>\n\n");
		for (SupportTicketEntity ticket : tickets) {
			text.append("#").append(ticket.getId()).append(' ')
					.append(ticket.getCategory().getEmoji()).append(' ')
					.append(escape(ticket.getUserDisplayName())).append(" — ")
					.append(statusLabel(ticket.getStatus())).append(", ")
					.append(ago(ticket.getUpdatedAt(), now)).append('\n');
		}
		text.append("\nНажмите на номер, чтобы открыть карточку и ответить.");
		return text.toString();
	}

	private static String statusLabel(TicketStatus status) {
		return switch (status) {
			case OPEN -> "⏳ ждёт ответа";
			case ANSWERED -> "💬 ждём пользователя";
			case CLOSED -> "✅ закрыто";
		};
	}

	private static String userLink(SupportTicketEntity ticket) {
		return "<a href=\"tg://user?id=" + ticket.getChatId() + "\">" + escape(ticket.getUserDisplayName()) + "</a>";
	}

	static String ago(Instant time, Instant now) {
		Duration duration = Duration.between(time, now);
		if (duration.toMinutes() < 1) {
			return "только что";
		}
		if (duration.toHours() < 1) {
			return duration.toMinutes() + " мин назад";
		}
		if (duration.toDays() < 1) {
			return duration.toHours() + " ч назад";
		}
		return duration.toDays() + " дн назад";
	}
}
