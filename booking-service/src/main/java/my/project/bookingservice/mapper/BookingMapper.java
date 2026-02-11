package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.client.BookingDishDto;
import my.project.bookingservice.dto.client.BookingTableDto;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(
		componentModel = "spring",
		uses = {
				DishMapper.class,
				TableMapper.class,
		}
)
public interface BookingMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "cancelledAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "table", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	@Mapping(target = "tableId", source = "req.tableId")
	BookingEntity toEntity(CreateBookingRequest req, UUID userId,
						   BookingTableDto table, List<BookingDishDto> dishes,
						   @Context Map<UUID, Integer> qty);

	@AfterMapping
	default void fillDetails(@MappingTarget BookingEntity entity,
							 CreateBookingRequest req, UUID userId,
							 BookingTableDto table, List<BookingDishDto> dishes,
							 @Context Map<UUID, Integer> qty,
							 DishMapper dishMapper, TableMapper tableMapper) {
		entity.setTable(tableMapper.toEntity(table));
		if (dishes != null) {
			dishes.stream().map(dishMapper::toEntity).forEach(d -> {
				d.setQuantity(qty.getOrDefault(d.getDishId(), 1));
				entity.addDish(d);
			});
		}
	}


	BookingResponse toResponse(BookingEntity entity);

	List<BookingResponse> toResponse(List<BookingEntity> entities);

}
