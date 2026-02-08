package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.service.DishService;
import my.project.restaurantservice.service.ManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalController {

	private final ManagerService managerService;
	private final DishService dishService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/api/v1/restaurants/{restId}/manager-access")
	public ResponseEntity<Boolean> managerHasAccess(@PathVariable UUID restId, Authentication auth) {
		var managerId = AuthUtil.id(auth);
		return ResponseEntity.ok(managerService.managerHasAccess(restId, managerId));
	}

	@PostMapping("/api/v1/restaurants/{restId}/dishes/ids")
	public ResponseEntity<List<DishDto>> findRestaurantBookingDishes(@PathVariable UUID restId,
																	 @RequestBody Set<UUID> ids) {
		return ResponseEntity.ok(dishService.findRestaurantBookingDishes(restId, ids));
	}


}
