package my.project.telegrambotservice.support.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketCategory {

	BUG("🐞", "Ошибка на сайте"),
	BOOKING("❓", "Вопрос о бронировании"),
	IDEA("💡", "Идея или пожелание"),
	RESTAURANT("🏢", "Вопрос от ресторана");

	private final String emoji;
	private final String title;

	public String label() {
		return emoji + " " + title;
	}
}
