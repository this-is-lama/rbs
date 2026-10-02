package my.project.restaurantservice.contact.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ContactsUpdateRequest(

		@NotEmpty
		List<@Valid ContactDto> contacts
) {}
