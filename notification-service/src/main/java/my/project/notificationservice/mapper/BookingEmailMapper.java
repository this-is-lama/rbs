package my.project.notificationservice.mapper;

import my.project.notificationservice.dto.BookingEmailDto;
import org.mapstruct.Mapper;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface BookingEmailMapper {

	Map<String, Object> toContext(BookingEmailDto dto);

}
