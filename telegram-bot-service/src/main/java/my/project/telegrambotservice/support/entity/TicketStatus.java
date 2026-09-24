package my.project.telegrambotservice.support.entity;

public enum TicketStatus {

	/** Новое обращение или пользователь дописал — ждёт ответа поддержки. */
	OPEN,
	/** Поддержка ответила — ждём пользователя. */
	ANSWERED,
	CLOSED;

	public boolean isActive() {
		return this != CLOSED;
	}
}
