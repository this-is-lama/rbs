package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.client.BookingSnapshotRequest;
import my.project.restaurantservice.dto.client.BookingSnapshotResponse;
import my.project.restaurantservice.service.ManagerService;
import my.project.restaurantservice.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalController {

	private final ManagerService managerService;
	private final RestaurantService restaurantService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/api/v1/restaurants/{restId}/manager-access")
	public ResponseEntity<Boolean> managerHasAccess(@PathVariable UUID restId, Authentication auth) {
		var managerId = AuthUtil.id(auth);
		return ResponseEntity.ok(managerService.managerHasAccess(restId, managerId));
	}

	@PostMapping("/api/v1/restaurants/{restId}/booking-snapshot")
	public ResponseEntity<BookingSnapshotResponse> bookingSnapshot(@PathVariable UUID restId,
																   @RequestBody @Valid BookingSnapshotRequest req) {
		return ResponseEntity.ok(restaurantService.bookingSnapshot(restId, req));
	}


}
