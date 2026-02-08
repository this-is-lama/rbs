package my.project.restaurantservice.dto.manager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddManagerRequest(

		@NotBlank
		@Email
		String email
) {}
