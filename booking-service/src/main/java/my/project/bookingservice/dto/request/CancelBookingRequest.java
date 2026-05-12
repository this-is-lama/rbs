package my.project.bookingservice.dto.request;

import jakarta.validation.constraints.Size;

public record CancelBookingRequest(

		@Size(max = 500)
		String reason

) {}