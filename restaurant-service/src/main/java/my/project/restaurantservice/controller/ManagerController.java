package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.manager.AddManagerRequest;
import my.project.restaurantservice.service.manager.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
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
		log.info("Получен запрос на добавление менеджера к ресторану, restId={}, email={}", restId, req.email());
		UUID managerId = managerService.addManagerByEmail(restId, req, auth);
		log.info("Менеджер успешно добавлен к ресторану, restId={}, managerId={}", restId, managerId);
		return ResponseEntity.ok(managerId);
	}
}