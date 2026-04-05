package my.project.notificationservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.events.BookingCreatedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageJsonMapper {

	private final ObjectMapper objectMapper;

	public String writeJson(BookingCreatedEvent event) {
		try {
			log.debug("Сериализация события в JSON, bookingId={}", event.bookingId());
			return objectMapper.writeValueAsString(event);
		} catch (Exception e) {
			log.error("Не удалось сериализовать событие, bookingId={}", event.bookingId(), e);
			throw new IllegalStateException("Cannot serialize event", e);
		}
	}

	public BookingCreatedEvent readJson(String json) {
		try {
			log.debug("Десериализация события из JSON");
			return objectMapper.readValue(json, BookingCreatedEvent.class);
		} catch (JsonProcessingException e) {
			log.error("Не удалось десериализовать событие из JSON", e);
			throw new IllegalStateException("Cannot deserialize event", e);
		}
	}
}