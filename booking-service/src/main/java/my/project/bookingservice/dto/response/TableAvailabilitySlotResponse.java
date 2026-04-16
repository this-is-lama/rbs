package my.project.bookingservice.dto.response;

import java.time.Instant;

public record TableAvailabilitySlotResponse(
        Instant startAt,
        Instant endAt
) {}
