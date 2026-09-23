package my.project.bookingservice.mapper;

import my.project.bookingservice.dto.client.UserDto;
import my.project.bookingservice.dto.events.BookingCancelledEvent;
import my.project.bookingservice.dto.events.BookingCreatedEvent;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        uses = {
                DishMapper.class,
                TableMapper.class,
                RestaurantMapper.class
        }
)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "preorderAmount", ignore = true)

    @Mapping(target = "table", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "dishes", ignore = true)

    @Mapping(target = "tableId", source = "req.tableId")
    @Mapping(target = "restaurantId", source = "req.restaurantId")
    @Mapping(target = "userId", source = "userId")
    BookingEntity toEntity(CreateBookingRequest req, UUID userId);

    BookingResponse toResponse(BookingEntity entity);

    List<BookingResponse> toResponse(List<BookingEntity> entities);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "restaurantName", source = "booking.restaurant.name")
    @Mapping(target = "restaurantDescription", source = "booking.restaurant.description")
    @Mapping(target = "restaurantAddress", source = "booking.restaurant.address")
    @Mapping(target = "tableNumber", source = "booking.table.tableNumber")
    @Mapping(target = "tableDescription", source = "booking.table.description")
    @Mapping(target = "totalAmount", source = "booking.totalAmount")
    @Mapping(target = "preorderAmount", source = "booking.preorderAmount")
    BookingCreatedEvent toEvent(BookingResponse booking, String email, String username);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "restaurantName", source = "booking.restaurant.name")
    @Mapping(target = "restaurantDescription", source = "booking.restaurant.description")
    @Mapping(target = "restaurantAddress", source = "booking.restaurant.address")
    @Mapping(target = "tableNumber", source = "booking.table.tableNumber")
    @Mapping(target = "tableDescription", source = "booking.table.description")
    @Mapping(target = "reason", source = "reason")
    BookingCancelledEvent toCancelledEvent(BookingResponse booking, String email, String username, String reason);

    @Mapping(target = "id", source = "booking.id")
    @Mapping(target = "restaurantId", source = "booking.restaurantId")
    @Mapping(target = "userId", source = "booking.userId")
    @Mapping(target = "startAt", source = "booking.startAt")
    @Mapping(target = "endAt", source = "booking.endAt")
    @Mapping(target = "status", source = "booking.status")
    @Mapping(target = "guests", source = "booking.guests")
    @Mapping(target = "comment", source = "booking.comment")
    @Mapping(target = "totalAmount", source = "booking.totalAmount")
    @Mapping(target = "preorderAmount", source = "booking.preorderAmount")
    @Mapping(target = "createdAt", source = "booking.createdAt")
    @Mapping(target = "cancelledAt", source = "booking.cancelledAt")
    @Mapping(target = "cancellationReason", source = "booking.cancellationReason")
    @Mapping(target = "restaurant", source = "booking.restaurant")
    @Mapping(target = "table", source = "booking.table")
    @Mapping(target = "dishes", source = "booking.dishes")
    @Mapping(target = "user", source = "user")
    ManagerBookingResponse toManagerResponse(BookingResponse booking, UserDto user);
}
