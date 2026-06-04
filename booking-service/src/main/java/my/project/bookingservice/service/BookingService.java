package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
	private final BookingCreationService creationService;
	private final BookingCancellationService cancellationService;

	@Transactional
	public BookingResponse create(CreateBookingRequest request, Authentication auth) {
		return creationService.create(request, auth);
	}

	@Transactional
	public void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth) {
		cancellationService.cancel(bookingId, request, auth);
	}
}
