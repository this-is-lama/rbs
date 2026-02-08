package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.manager.AddManagerRequest;
import my.project.restaurantservice.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class ManagerController {

	private final ManagerService managerService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/{restId}/managers")
	public ResponseEntity<UUID> addManagerByEmail(@PathVariable UUID restId,
												  @RequestBody AddManagerRequest req,
												  Authentication auth) {
		return ResponseEntity.ok(managerService.addManagerByEmail(restId, req, auth));
	}

}
