package my.project.telegrambotservice.support.service;

import my.project.telegrambotservice.support.entity.TicketCategory;

/**
 * Данные заполненного черновика, из которых создаётся обращение.
 */
public record NewTicket(
		long chatId,
		String userDisplayName,
		String username,
		TicketCategory category,
		String description,
		String restaurant,
		String email
) {
}
