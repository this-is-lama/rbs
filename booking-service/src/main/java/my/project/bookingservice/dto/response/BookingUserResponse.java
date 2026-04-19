package my.project.bookingservice.dto.response;

import java.util.UUID;

public record BookingUserResponse(
        UUID id,
        String name,
        String surname,
        String email,
        String phone,
        boolean active
) {
}