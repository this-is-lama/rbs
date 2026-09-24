package my.project.telegrambotservice.telegram.polling;

import my.project.telegrambotservice.telegram.dto.Update;

/**
 * Тот, кто обрабатывает обновления, полученные {@link UpdatePoller}.
 * Благодаря этому интерфейсу пакет telegram ничего не знает о логике бота.
 */
public interface UpdateHandler {

	void handle(Update update);
}
