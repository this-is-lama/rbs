package my.project.notificationservice.service;

import jakarta.mail.MessagingException;
import my.project.notificationservice.events.BookingNotificationEvent;

public interface SenderService {

	void sendMessage(BookingNotificationEvent event) throws MessagingException;
}
