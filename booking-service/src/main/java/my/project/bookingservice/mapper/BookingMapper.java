package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.DishDto;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(
		componentModel = "spring",
		uses = { BookingDishMapper.class }
)
public interface BookingMapper {


	BookingResponse toResponse(BookingEntity entity);


	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "cancelledAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	BookingEntity toEntity(CreateBookingRequest req,
						   UUID userId);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "cancelledAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "dishes", ignore = true)
	BookingEntity toEntity(CreateBookingRequest req,
						   UUID userId,
						   @Context Map<UUID, DishDto> dishesSnapshot);

	List<BookingResponse> toResponse(List<BookingEntity> entities);

	@AfterMapping
	default void fillDishes(CreateBookingRequest req,
							@MappingTarget BookingEntity booking,
							@Context Map<UUID, DishDto> dishesSnapshot,
							BookingDishMapper dishMapper) {
		if (req.dishes() == null || req.dishes().isEmpty()) return;
		req.dishes().forEach(d -> booking.addDish(dishMapper.toEntity(d, dishesSnapshot)));
	}
}
