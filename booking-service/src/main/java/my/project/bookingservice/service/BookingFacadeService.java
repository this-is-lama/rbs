package my.project.bookingservice.service;

import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.UUID;

public interface BookingFacadeService {

	BookingResponse create(CreateBookingRequest req, Authentication auth);

	void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth);

	TableAvailabilityResponse getPublicTableAvailability(UUID restId,
																UUID tableId,
																LocalDate date);
}
