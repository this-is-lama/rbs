package my.project.notificationservice.events;

import my.project.notificationservice.entity.MessageType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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