package my.project.notificationservice.mapper;

import my.project.notificationservice.events.BookingCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface MailContextMapper {

	@Mapping(target = "userName", source = "email")
	Map<String, Object> toContext(BookingCreatedEvent event);
}
