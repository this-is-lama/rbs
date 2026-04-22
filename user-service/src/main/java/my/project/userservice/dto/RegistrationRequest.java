package my.project.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import my.project.common.security.UserRole;

import java.time.LocalDate;

public record RegistrationRequest(

		@NotBlank
		String name,

		@NotBlank
		String surname,

		LocalDate dateOfBirth,

		String phone,

		@NotBlank
		@Email
		String email,

		@NotBlank
		String password,

		@NotNull
		UserRole role
) {}