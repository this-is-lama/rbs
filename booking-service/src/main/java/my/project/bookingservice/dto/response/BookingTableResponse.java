package my.project.bookingservice.dto.response;

import java.util.UUID;

public record BookingTableResponse(

		UUID id,

		UUID tableId,

		Integer tableNumber,

		String description,

		Integer capacity

) {}