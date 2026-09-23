package my.project.restaurantservice.manager.dto;

import jakarta.validation.constraints.NotNull;
import my.project.common.security.UserRole;

import java.util.UUID;

public record ChangeRoleByIdRequest(

        @NotNull
        UUID userId,

        @NotNull
        UserRole role
) {}