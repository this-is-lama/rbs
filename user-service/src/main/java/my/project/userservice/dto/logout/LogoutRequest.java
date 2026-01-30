package my.project.userservice.dto.logout;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(

		@NotBlank
		String refreshToken
) {}
