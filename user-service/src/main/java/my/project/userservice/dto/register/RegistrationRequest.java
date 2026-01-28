package my.project.userservice.dto.register;

import java.time.LocalDate;

public record RegistrationRequest(
		String name,
		String surname,
		LocalDate dateOfBirth,
		String phone,
		String email,
		String password
) {}