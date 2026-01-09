package my.project.userservice.dto;

import java.time.LocalDate;

public record RegistrationRequest(
		String name,
		String surname,
		LocalDate dateOfBirth,
		String phone,
		String email,
		String password
) {}