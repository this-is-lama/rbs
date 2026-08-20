package my.project.bookingservice.service.usecase;

import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import org.springframework.security.core.Authentication;

public interface BookingCreateUseCase {

	BookingResponse create(CreateBookingRequest req, Authentication auth);
}
