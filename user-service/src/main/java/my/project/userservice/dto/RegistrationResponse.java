package my.project.userservice.dto;

import java.util.UUID;

public record RegistrationResponse(UUID id, String email) {}