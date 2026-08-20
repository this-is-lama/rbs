package my.project.bookingservice.dto.client;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
		UUID id,
        String name,
        String surname,
        LocalDate dateOfBirth,
        String phone,
        String email,
        String role,
        boolean enabled
) {}
