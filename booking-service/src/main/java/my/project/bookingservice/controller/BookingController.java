package my.project.bookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<?> findById(@PathVariable UUID id) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/me")
	public ResponseEntity<?> findMyBookings() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<?> cancel(@PathVariable UUID id) {
		return ResponseEntity.ok().build();
	}


	@GetMapping("/manager/restaurants/{restId}")
	public ResponseEntity<?> restaurantBookings(@PathVariable UUID restId) {
		return ResponseEntity.ok().build();
	}

}
