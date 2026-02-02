package my.project.userservice.dto;

import java.time.Instant;
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
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
