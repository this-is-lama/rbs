package my.project.telegrambotservice.bot;

/**
 * Сообщения бот отправляет с parse_mode=HTML, поэтому любой текст пользователя нужно экранировать.
 */
public final class Html {

	private Html() {
	}

	public static String escape(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	public static String truncate(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength - 1) + "…";
	}
}
