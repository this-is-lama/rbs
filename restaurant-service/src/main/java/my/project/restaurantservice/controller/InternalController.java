package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.client.BookingSnapshotRequest;
import my.project.restaurantservice.dto.client.BookingSnapshotResponse;
import my.project.restaurantservice.service.manager.ManagerPersistenceService;
import my.project.restaurantservice.service.restaurant.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalController {

	private final ManagerPersistenceService persistenceService;
	private final RestaurantService restaurantService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/api/v1/restaurants/{restId}/manager-access")
	public ResponseEntity<Boolean> managerHasAccess(@PathVariable UUID restId, Authentication auth) {
		var managerId = AuthUtil.id(auth);
		log.info("Получен запрос на проверку доступа менеджера, restId={}, managerId={}", restId, managerId);
		return ResponseEntity.ok(persistenceService.managerHasAccess(restId, managerId));
	}

	@PostMapping("/api/v1/restaurants/{restId}/booking-snapshot")
	public ResponseEntity<BookingSnapshotResponse> bookingSnapshot(@PathVariable UUID restId,
																   @RequestBody @Valid BookingSnapshotRequest req) {
		log.info("Получен запрос на snapshot для бронирования, restId={}, tableId={}", restId, req.tableId());
		return ResponseEntity.ok(restaurantService.bookingSnapshot(restId, req));
	}

}
