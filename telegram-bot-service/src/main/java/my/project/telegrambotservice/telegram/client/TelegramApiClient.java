package my.project.telegrambotservice.telegram.client;

import lombok.extern.slf4j.Slf4j;
import my.project.telegrambotservice.config.TelegramProperties;
import my.project.telegrambotservice.telegram.dto.ApiResponse;
import my.project.telegrambotservice.telegram.dto.BotCommand;
import my.project.telegrambotservice.telegram.dto.BotCommandScope;
import my.project.telegrambotservice.telegram.dto.CopyMessageRequest;
import my.project.telegrambotservice.telegram.dto.InlineKeyboardMarkup;
import my.project.telegrambotservice.telegram.dto.Message;
import my.project.telegrambotservice.telegram.dto.MessageId;
import my.project.telegrambotservice.telegram.dto.SendMessageRequest;
import my.project.telegrambotservice.telegram.dto.Update;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Тонкий клиент Bot API поверх RestClient: боту нужно всего несколько методов,
 * поэтому отдельная библиотека не подключается.
 */
@Slf4j
@Component
public class TelegramApiClient implements TelegramApi {

	private static final ParameterizedTypeReference<ApiResponse<List<Update>>> UPDATES = new ParameterizedTypeReference<>() {};
	private static final ParameterizedTypeReference<ApiResponse<Message>> MESSAGE = new ParameterizedTypeReference<>() {};
	private static final ParameterizedTypeReference<ApiResponse<MessageId>> MESSAGE_ID = new ParameterizedTypeReference<>() {};
	private static final ParameterizedTypeReference<ApiResponse<Object>> ANY = new ParameterizedTypeReference<>() {};

	private final RestClient restClient;
	private final String token;

	public TelegramApiClient(RestClient telegramRestClient, TelegramProperties properties) {
		this.restClient = telegramRestClient;
		this.token = properties.token();
	}

	@Override
	public List<Update> getUpdates(long offset, int timeoutSeconds) {
		Map<String, Object> body = Map.of(
				"offset", offset,
				"timeout", timeoutSeconds,
				"allowed_updates", List.of("message", "callback_query")
		);
		List<Update> updates = call("getUpdates", body, UPDATES);
		return updates == null ? List.of() : updates;
	}

	@Override
	public Message sendMessage(SendMessageRequest request) {
		return call("sendMessage", request, MESSAGE);
	}

	@Override
	public long copyMessage(CopyMessageRequest request) {
		return call("copyMessage", request, MESSAGE_ID).messageId();
	}

	@Override
	public void answerCallbackQuery(String callbackQueryId, String text) {
		Map<String, Object> body = new HashMap<>();
		body.put("callback_query_id", callbackQueryId);
		if (text != null) {
			body.put("text", text);
		}
		call("answerCallbackQuery", body, ANY);
	}

	@Override
	public void editMessageReplyMarkup(long chatId, long messageId, InlineKeyboardMarkup markup) {
		Map<String, Object> body = Map.of(
				"chat_id", chatId,
				"message_id", messageId,
				"reply_markup", markup
		);
		call("editMessageReplyMarkup", body, ANY);
	}

	@Override
	public void setMessageReaction(long chatId, long messageId, String emoji) {
		Map<String, Object> body = Map.of(
				"chat_id", chatId,
				"message_id", messageId,
				"reaction", List.of(Map.of("type", "emoji", "emoji", emoji))
		);
		call("setMessageReaction", body, ANY);
	}

	@Override
	public void setMyCommands(List<BotCommand> commands, BotCommandScope scope) {
		Map<String, Object> body = Map.of(
				"commands", commands,
				"scope", scope
		);
		call("setMyCommands", body, ANY);
	}

	private <T> T call(String method, Object body, ParameterizedTypeReference<ApiResponse<T>> type) {
		ApiResponse<T> response;
		try {
			response = restClient.post()
					.uri("/{method}", method)
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					// при ошибке Telegram возвращает тот же JSON с ok=false — разбираем его ниже, а не бросаем исключение RestClient
					.onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
					})
					.body(type);
		} catch (RestClientException e) {
			// в тексте исключения RestClient есть URL, а в URL — токен бота; в логи его пускать нельзя
			throw new TelegramApiException(method, 0, hideToken(e.getMessage()));
		}

		if (response == null) {
			throw new TelegramApiException(method, 0, "пустой ответ");
		}
		if (!response.ok()) {
			int code = response.errorCode() == null ? 0 : response.errorCode();
			throw new TelegramApiException(method, code, response.description());
		}
		return response.result();
	}

	private String hideToken(String text) {
		if (text == null || token == null || token.isEmpty()) {
			return text;
		}
		return text.replace(token, "***");
	}
}
