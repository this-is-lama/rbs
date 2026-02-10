package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.DishDto;
import my.project.bookingservice.dto.TableDto;
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


	BookingResponse toResponse(BookingEntity entity);


	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "cancelledAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "table", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	BookingEntity toEntity(CreateBookingRequest req, UUID userId,
						   TableDto table, List<DishDto> dishes,
						   @Context Map<UUID, Integer> qty);

	@AfterMapping
	default void fillDetails(@MappingTarget BookingEntity entity,
							 CreateBookingRequest req, UUID userId,
							 TableDto table, List<DishDto> dishes,
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


	List<BookingResponse> toResponse(List<BookingEntity> entities);

}
