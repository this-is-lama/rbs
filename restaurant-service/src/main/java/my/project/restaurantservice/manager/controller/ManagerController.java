package my.project.restaurantservice.manager.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.manager.dto.RestaurantManagerDto;
import my.project.restaurantservice.manager.service.query.ManagerDetailsService;
import my.project.restaurantservice.manager.service.command.ManagerCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class ManagerController {

	private final ManagerCommandService commandService;
	private final ManagerDetailsService detailsService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/{restId}/managers/{managerId}")
	public ResponseEntity<UUID> addManagerById(@PathVariable UUID restId,
											   @PathVariable UUID managerId,
											   Authentication auth) {
		UUID addedManagerId = commandService.addManagerById(restId, managerId, auth);
		return ResponseEntity.ok(addedManagerId);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/{restId}/managers")
	public ResponseEntity<List<RestaurantManagerDto>> findAll(@PathVariable UUID restId,
															  Authentication auth) {
		return ResponseEntity.ok(detailsService.findAllManagersByRestaurantId(restId, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{restId}/managers/{managerId}")
	public ResponseEntity<Void> deleteManagerById(@PathVariable UUID restId,
												  @PathVariable UUID managerId,
												  Authentication auth) {
		commandService.deleteManagerById(restId, managerId, auth);
		return ResponseEntity.noContent().build();
	}
}