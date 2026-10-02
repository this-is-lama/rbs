package my.project.restaurantservice.workinghours.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WorkingHoursUpdateRequest(

		@NotEmpty
		List<@Valid WorkingHoursDto> workingHours
) {}
