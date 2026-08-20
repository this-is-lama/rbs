package my.project.bookingservice.service.usecase;

import my.project.bookingservice.dto.response.TableAvailabilityResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface BookingTableAvailabilityUseCase {

	TableAvailabilityResponse getPublicTableAvailability(UUID restId,
														 UUID tableId,
														 LocalDate date);
}
