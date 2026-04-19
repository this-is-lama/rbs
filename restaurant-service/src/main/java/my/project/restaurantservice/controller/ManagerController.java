package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.manager.RestaurantManagerDto;
import my.project.restaurantservice.service.manager.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class ManagerController {

	private final ManagerService managerService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/{restId}/managers/{managerId}")
	public ResponseEntity<UUID> addManagerById(@PathVariable UUID restId,
											   @PathVariable UUID managerId,
											   Authentication auth) {
		log.info("Получен запрос на добавление менеджера к ресторану, restId={}, managerId={}", restId, managerId);
		UUID addedManagerId = managerService.addManagerById(restId, managerId, auth);
		log.info("Менеджер успешно добавлен к ресторану, restId={}, managerId={}", restId, addedManagerId);
		return ResponseEntity.ok(addedManagerId);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/{restId}/managers")
	public ResponseEntity<List<RestaurantManagerDto>> findAll(@PathVariable UUID restId,
															  Authentication auth) {
		log.info("Получен запрос на список менеджеров ресторана, restId={}", restId);
		return ResponseEntity.ok(managerService.findAllByRestaurantId(restId, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{restId}/managers/{managerId}")
	public ResponseEntity<Void> deleteManagerById(@PathVariable UUID restId,
												  @PathVariable UUID managerId,
												  Authentication auth) {
		log.info("Получен запрос на удаление менеджера ресторана, restId={}, managerId={}", restId, managerId);
		managerService.deleteManagerById(restId, managerId, auth);
		log.info("Менеджер успешно удалён из ресторана, restId={}, managerId={}", restId, managerId);
		return ResponseEntity.noContent().build();
	}
}