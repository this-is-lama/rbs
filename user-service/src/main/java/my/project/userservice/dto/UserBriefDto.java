package my.project.userservice.dto;

import java.util.UUID;

public record UserBriefDto(
        UUID id,
        String name,
        String surname,
        String email,
        String phone,
        boolean enabled
) {
}