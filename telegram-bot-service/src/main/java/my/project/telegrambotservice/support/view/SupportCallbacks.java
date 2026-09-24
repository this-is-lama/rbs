package my.project.telegrambotservice.support.view;

/**
 * Данные inline-кнопок (callback_data, до 64 байт).
 * Кнопки чата поддержки начинаются с {@link #SUPPORT_PREFIX} и принимаются только из чата поддержки.
 */
public final class SupportCallbacks {

	/** cat:BUG — выбор темы */
	public static final String CATEGORY = "cat";
	/** skip:ATTACHMENTS / skip:EMAIL — «Пропустить» или «Готово» на необязательном шаге */
	public static final String SKIP = "skip";
	public static final String SEND = "send";
	public static final String EDIT = "edit";
	public static final String CANCEL = "cancel";
	public static final String NEW = "new";
	/** rate:12:HELPFUL — оценка закрытого обращения */
	public static final String RATE = "rate";

	public static final String SUPPORT_PREFIX = "adm:";
	/** adm:close:12 */
	public static final String CLOSE = "close";
	/** adm:show:12 */
	public static final String SHOW = "show";

	private SupportCallbacks() {
	}

	public static String of(String... parts) {
		return String.join(":", parts);
	}

	public static String support(String action, long ticketId) {
		return SUPPORT_PREFIX + action + ":" + ticketId;
	}
}
