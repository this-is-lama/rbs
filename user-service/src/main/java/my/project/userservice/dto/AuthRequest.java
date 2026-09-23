package my.project.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

		@NotBlank
		@Email
		String email,

		@NotBlank
		String password
) {

	@Override
	public String toString() {
		return "AuthRequest[email=" + email + ", password=***]";
	}
}