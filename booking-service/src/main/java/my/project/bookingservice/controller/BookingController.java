package my.project.bookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;


	@PostMapping
	public ResponseEntity<BookingResponse> create(@RequestBody @Valid CreateBookingRequest req,
												  Authentication auth) {
		return ResponseEntity.ok(bookingService.create(req, auth));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookingResponse> findById(@PathVariable UUID id, Authentication auth) {
		return ResponseEntity.ok(bookingService.findById(id, auth));
	}

	@GetMapping("/me")
	public ResponseEntity<List<BookingResponse>> findUserBookings(Authentication auth) {
		return ResponseEntity.ok(bookingService.findUserBookings(auth));
	}

	@DeleteMapping("/{id}/cancel")
	public ResponseEntity<Void> cancel(@PathVariable UUID id, Authentication auth) {
		bookingService.cancel(id, auth);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
	@GetMapping("/manager/restaurants/{restId}")
	public ResponseEntity<List<BookingResponse>> restaurantBookings(@PathVariable UUID restId, Authentication auth) {
		return ResponseEntity.ok(bookingService.findAllByRestaurantId(restId, auth));
	}

}
