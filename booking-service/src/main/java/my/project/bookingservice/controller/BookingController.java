package my.project.bookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.service.command.BookingCommandService;
import my.project.bookingservice.service.query.BookingDetailsService;
import my.project.bookingservice.service.query.BookingQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingCommandService commandService;
	private final BookingDetailsService detailsService;
	private final BookingQueryService queryService;

	@PostMapping
	public ResponseEntity<BookingResponse> create(@RequestBody @Valid CreateBookingRequest req,
												  Authentication auth) {
		return ResponseEntity.ok(commandService.create(req, auth));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookingResponse> findById(@PathVariable UUID id, Authentication auth) {
		return ResponseEntity.ok(detailsService.findById(id, auth));
	}

	@GetMapping("/me")
	public ResponseEntity<List<BookingResponse>> findUserBookings(Authentication auth) {
		return ResponseEntity.ok(detailsService.findUserBookings(auth));
	}

	@DeleteMapping("/{id}/cancel")
	public ResponseEntity<Void> cancel(@PathVariable UUID id,
									   @RequestBody(required = false) @Valid CancelBookingRequest req,
									   Authentication auth) {
		commandService.cancel(id, req, auth);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
	@GetMapping("/manager/restaurants/{restId}")
	public ResponseEntity<List<ManagerBookingResponse>> restaurantBookings(@PathVariable UUID restId,
																		   Authentication auth) {
		return ResponseEntity.ok(detailsService.findAllByRestaurantId(restId, auth));
	}

	@GetMapping("/public/restaurants/{restaurantId}/tables/{tableId}/availability")
	public ResponseEntity<TableAvailabilityResponse> getPublicTableAvailability(@PathVariable UUID restaurantId,
																				@PathVariable UUID tableId,
																				@RequestParam
																				@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
																				LocalDate date) {
		return ResponseEntity.ok(queryService.getTableAvailability(restaurantId, tableId, date));
	}
}
