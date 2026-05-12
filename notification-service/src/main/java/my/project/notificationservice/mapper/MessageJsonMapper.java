package my.project.notificationservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageType;
import my.project.notificationservice.events.BookingCancelledEvent;
import my.project.notificationservice.events.BookingCreatedEvent;
import my.project.notificationservice.events.BookingNotificationEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageJsonMapper {

	private final ObjectMapper objectMapper;

	public String writeJson(BookingNotificationEvent event) {
		try {
			log.debug("Сериализация события в JSON, bookingId={}, messageType={}",
					event.bookingId(), event.messageType());
			return objectMapper.writeValueAsString(event);
		} catch (Exception e) {
			log.error("Не удалось сериализовать событие, bookingId={}, messageType={}",
					event.bookingId(), event.messageType(), e);
			throw new IllegalStateException("Cannot serialize event", e);
		}
	}

	public BookingNotificationEvent readJson(MessageType messageType, String json) {
		try {
			log.debug("Десериализация события из JSON, messageType={}", messageType);

			return switch (messageType) {
				case BOOKING_CREATED -> objectMapper.readValue(json, BookingCreatedEvent.class);
				case BOOKING_CANCELLED -> objectMapper.readValue(json, BookingCancelledEvent.class);
			};
		} catch (JsonProcessingException e) {
			log.error("Не удалось десериализовать событие из JSON, messageType={}", messageType, e);
			throw new IllegalStateException("Cannot deserialize event", e);
		}
	}
}