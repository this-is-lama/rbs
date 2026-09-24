package my.project.telegrambotservice.telegram.client;

import my.project.telegrambotservice.telegram.dto.BotCommand;
import my.project.telegrambotservice.telegram.dto.BotCommandScope;
import my.project.telegrambotservice.telegram.dto.CopyMessageRequest;
import my.project.telegrambotservice.telegram.dto.InlineKeyboardMarkup;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.SendMessageRequest;
import my.project.telegrambotservice.telegram.dto.Update;

import java.util.List;

/**
 * Методы Telegram Bot API, которые нужны боту. Описание методов: https://core.telegram.org/bots/api
 */
public interface TelegramApi {

	List<Update> getUpdates(long offset, int timeoutSeconds);

	Message sendMessage(SendMessageRequest request);

	/** @return id копии в чате-получателе */
	long copyMessage(CopyMessageRequest request);

	void answerCallbackQuery(String callbackQueryId, String text);

	void editMessageReplyMarkup(long chatId, long messageId, InlineKeyboardMarkup markup);

	void setMessageReaction(long chatId, long messageId, String emoji);

	void setMyCommands(List<BotCommand> commands, BotCommandScope scope);
}
