package my.project.userservice.dto.register;

import java.util.UUID;

public record RegistrationResponse(UUID id, String email) {}