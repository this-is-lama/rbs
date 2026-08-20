package my.project.bookingservice.service.usecase;

import my.project.bookingservice.dto.request.CancelBookingRequest;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface BookingCancelUseCase {

	void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth);
}
