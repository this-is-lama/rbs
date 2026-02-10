package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.TableDto;
import my.project.bookingservice.dto.response.BookingTableResponse;
import my.project.bookingservice.entity.TableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "booking", ignore = true)
	@Mapping(target = "tableId", source = "id")
	TableEntity toEntity(TableDto dto);

	BookingTableResponse toResponse(TableEntity entity);

}
