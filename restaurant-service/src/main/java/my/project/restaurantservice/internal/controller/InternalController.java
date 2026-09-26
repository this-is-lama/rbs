package my.project.restaurantservice.internal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.internal.dto.BookingSnapshotRequest;
import my.project.restaurantservice.internal.dto.BookingSnapshotResponse;
import my.project.restaurantservice.manager.service.query.ManagerQueryService;
import my.project.restaurantservice.internal.service.BookingSnapshotDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalController {

	private final ManagerQueryService managerQueryService;
	private final BookingSnapshotDetailsService bookingSnapshotDetailsService;

	@Loggable
	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/api/v1/restaurants/{restId}/manager-access")
	public ResponseEntity<Boolean> managerHasAccess(@PathVariable UUID restId, Authentication auth) {
		return ResponseEntity.ok(managerQueryService.managerHasAccess(restId, AuthUtil.id(auth)));
	}

	@Loggable
	@PostMapping("/api/v1/restaurants/{restId}/booking-snapshot")
	public ResponseEntity<BookingSnapshotResponse> bookingSnapshot(@PathVariable UUID restId,
																   @RequestBody @Valid BookingSnapshotRequest req) {
		return ResponseEntity.ok(bookingSnapshotDetailsService.bookingSnapshot(restId, req));
	}

}
