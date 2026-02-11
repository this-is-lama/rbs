package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.client.BookingTableDto;
import my.project.bookingservice.dto.response.TableResponse;
import my.project.bookingservice.entity.TableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "booking", ignore = true)
	@Mapping(target = "tableId", source = "id")
	TableEntity toEntity(BookingTableDto dto);

	TableResponse toResponse(TableEntity entity);

}
