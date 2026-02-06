package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.dto.restaurant.AddManagerRequest;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantInfoDto;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.service.DishService;
import my.project.restaurantservice.service.ManagerService;
import my.project.restaurantservice.service.RestaurantService;
import my.project.restaurantservice.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;
	private final DishService dishService;
	private final TableService tableService;
	private final ManagerService managerService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping()
	public ResponseEntity<UUID> create(@Valid @RequestBody RestaurantDto dto,
									   Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(restaurantService.save(dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<RestaurantDto> update(@PathVariable UUID id,
												@RequestBody @Valid RestaurantDto dto,
												Authentication auth) {
		return ResponseEntity.ok(restaurantService.update(id, dto, auth));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantDto> findById(@PathVariable UUID id, Authentication auth) {
		return ResponseEntity.ok(restaurantService.findById(id, auth));
	}

	@GetMapping()
	public ResponseEntity<List<RestaurantInfoDto>> findAll() {
		return ResponseEntity.ok(restaurantService.findAll());
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
		restaurantService.delete(id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/{restId}/dishes")
	public ResponseEntity<UUID> addDish(@PathVariable UUID restId,
										@Valid @RequestBody DishDto dto,
										Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(dishService.save(dto, restId, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/{restId}/tables")
	public ResponseEntity<UUID> addTable(@PathVariable UUID restId,
										 @Valid @RequestBody TableDto dto,
										 Authentication auth) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(tableService.save(dto, restId, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/{restId}/managers")
	public ResponseEntity<UUID> addManager(@PathVariable UUID restId,
										   @RequestBody AddManagerRequest req,
										   Authentication auth) {
		return ResponseEntity.ok(managerService.addManager(restId, req, auth));
	}

	@GetMapping("/{restId}/manager-access")
	public ResponseEntity<Boolean> getManagerAccess(@PathVariable UUID restId, Authentication auth) {
		return ResponseEntity.ok(managerService.checkAccess(restId, auth));
	}

}
