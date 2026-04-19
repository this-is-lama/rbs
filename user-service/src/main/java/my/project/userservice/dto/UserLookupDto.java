package my.project.userservice.dto;

import java.util.UUID;

public record UserLookupDto(
        UUID id,
        String name,
        String surname,
        String email,
        String role,
        boolean enabled
) {}