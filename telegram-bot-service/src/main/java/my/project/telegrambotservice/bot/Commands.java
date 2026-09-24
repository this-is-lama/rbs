package my.project.telegrambotservice.bot;

import my.project.telegrambotservice.telegram.dto.BotCommand;
import my.project.telegrambotservice.telegram.dto.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Команды бота. Списки {@link #USER_MENU} и {@link #SUPPORT_MENU} бот сам регистрирует в Telegram при старте,
 * настраивать их в @BotFather не нужно.
 */
public final class Commands {

	public static final String START = "/start";
	public static final String HELP = "/help";
	public static final String FEEDBACK = "/feedback";
	public static final String NEW = "/new";
	public static final String RESTAURANT = "/restaurant";
	public static final String CANCEL = "/cancel";
	public static final String TICKETS = "/tickets";

	public static final List<BotCommand> USER_MENU = List.of(
			new BotCommand("feedback", "Написать в поддержку"),
			new BotCommand("restaurant", "Вопрос от ресторана"),
			new BotCommand("cancel", "Отменить заполнение обращения"),
			new BotCommand("help", "Как работает поддержка"),
			new BotCommand("start", "Начать сначала")
	);

	public static final List<BotCommand> SUPPORT_MENU = withTickets();

	private Commands() {
	}

	/**
	 * Команда из текста сообщения в нижнем регистре, без аргументов и без @имени_бота; {@code null}, если это не команда.
	 */
	public static String extract(Message message) {
		String text = message.text();
		if (text == null || !text.startsWith("/")) {
			return null;
		}
		String command = text.strip().split("\\s+", 2)[0];
		int at = command.indexOf('@');
		if (at > 0) {
			command = command.substring(0, at);
		}
		return command.toLowerCase(Locale.ROOT);
	}

	public static boolean is(Message message, String command) {
		return command.equals(extract(message));
	}

	private static List<BotCommand> withTickets() {
		List<BotCommand> commands = new ArrayList<>();
		commands.add(new BotCommand("tickets", "Открытые обращения"));
		commands.addAll(USER_MENU);
		return List.copyOf(commands);
	}
}
