package my.project.notificationservice.service;

import my.project.notificationservice.events.BookingNotificationEvent;

public interface NotificationService {

	void send(BookingNotificationEvent event);
}
