package my.project.notificationservice.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.OptBoolean;
import my.project.notificationservice.entity.MessageType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "messageType", requireTypeIdForSubtypes = OptBoolean.FALSE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BookingCreatedEvent.class, name = "BOOKING_CREATED"),
        @JsonSubTypes.Type(value = BookingCancelledEvent.class, name = "BOOKING_CANCELLED")
})
public interface BookingNotificationEvent {

    UUID bookingId();

    String email();

    String username();

    Instant startAt();

    Instant endAt();

    Integer guests();

    String comment();

    BigDecimal totalAmount();

    String restaurantName();

    String restaurantDescription();

    String restaurantAddress();

    Integer tableNumber();

    String tableDescription();

    MessageType messageType();
}