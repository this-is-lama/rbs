package my.project.userservice.dto;

import jakarta.validation.constraints.NotNull;
import my.project.common.security.UserRole;

import java.util.UUID;

public record ChangeRoleByIdRequest(

        @NotNull
        UUID userId,

        @NotNull
        UserRole role
) {}