package my.project.bookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.service.BookingReadService;
import my.project.bookingservice.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;
	private final BookingReadService bookingReadService;

	@PostMapping
	public ResponseEntity<BookingResponse> create(@RequestBody @Valid CreateBookingRequest req,
												  Authentication auth) {
		log.info("Получен запрос на создание бронирования, restaurantId={}, tableId={}",
				req.restaurantId(), req.tableId());
		return ResponseEntity.ok(bookingService.create(req, auth));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookingResponse> findById(@PathVariable UUID id, Authentication auth) {
		log.info("Получен запрос на получение бронирования, bookingId={}", id);
		return ResponseEntity.ok(bookingReadService.findById(id, auth));
	}

	@GetMapping("/me")
	public ResponseEntity<List<BookingResponse>> findUserBookings(Authentication auth) {
		log.info("Получен запрос на получение списка бронирований текущего пользователя");
		return ResponseEntity.ok(bookingReadService.findUserBookings(auth));
	}

	@DeleteMapping("/{id}/cancel")
	public ResponseEntity<Void> cancel(@PathVariable UUID id, Authentication auth) {
		log.info("Получен запрос на отмену бронирования, bookingId={}", id);
		bookingService.cancel(id, auth);
		log.info("Бронирование успешно отменено, bookingId={}", id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
	@GetMapping("/manager/restaurants/{restId}")
	public ResponseEntity<List<ManagerBookingResponse>> restaurantBookings(@PathVariable UUID restId,
																		   Authentication auth) {
		log.info("Получен запрос на список бронирований ресторана для менеджера, restId={}", restId);
		return ResponseEntity.ok(bookingReadService.findAllByRestaurantId(restId, auth));
	}

	@GetMapping("/public/restaurants/{restaurantId}/tables/{tableId}/availability")
	public ResponseEntity<TableAvailabilityResponse> getPublicTableAvailability(@PathVariable UUID restaurantId,
																				@PathVariable UUID tableId,
																				@RequestParam
																				@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
																				LocalDate date) {
		log.info("Получен запрос на публичную занятость стола, restaurantId={}, tableId={}, date={}",
				restaurantId, tableId, date);
		return ResponseEntity.ok(bookingReadService.getPublicTableAvailability(restaurantId, tableId, date));
	}
}