package my.project.bookingservice.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TableAvailabilityResponse(
        UUID restaurantId,
        UUID tableId,
        LocalDate date,
        List<TableAvailabilitySlotResponse> reservedSlots
) {}
