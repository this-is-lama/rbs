package my.project.notificationservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import my.project.notificationservice.events.BookingCreatedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageJsonMapper {

	private final ObjectMapper objectMapper;

	public String writeJson(BookingCreatedEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot serialize event", e);
		}
	}

	public BookingCreatedEvent readJson(String json) {
		try {
			return objectMapper.readValue(json, BookingCreatedEvent.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot deserialize event", e);
		}
	}
}
