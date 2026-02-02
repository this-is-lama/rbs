package my.project.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import my.project.common.security.UserRole;

public record ChangeRoleRequest(

		@NotBlank
		@Email
		String email,

		@NotNull
		UserRole role
) {}
