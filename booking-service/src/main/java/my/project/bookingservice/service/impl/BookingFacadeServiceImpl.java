package my.project.bookingservice.service.impl;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.service.BookingFacadeService;
import my.project.bookingservice.service.usecase.BookingCancelUseCase;
import my.project.bookingservice.service.usecase.BookingCreateUseCase;
import my.project.bookingservice.service.usecase.BookingTableAvailabilityUseCase;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingFacadeServiceImpl implements BookingFacadeService {

	private final BookingCreateUseCase createUseCase;
	private final BookingCancelUseCase cancelUseCase;
	private final BookingTableAvailabilityUseCase availabilityUseCase;

	public BookingResponse create(CreateBookingRequest req, Authentication auth) {
		return createUseCase.create(req, auth);
	}

	public void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth) {
		cancelUseCase.cancel(bookingId, request, auth);
	}

	public TableAvailabilityResponse getPublicTableAvailability(UUID restId,
																UUID tableId,
																LocalDate date) {
		return availabilityUseCase.getPublicTableAvailability(restId, tableId, date);
	}


}
