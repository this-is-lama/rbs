package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.restaurant.RestaurantCardDto;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.service.RestaurantService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;


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
	public ResponseEntity<Page<RestaurantCardDto>> findAll(@RequestParam(required = false) String category,
														   @RequestParam(required = false) String name,
														   @RequestParam(required = false) Boolean active,
														   @RequestParam(required = false) String address,
														   @RequestParam(defaultValue = "0") int page,
														   @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(restaurantService.findAll(category, name, active, address, page, size));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
		restaurantService.delete(id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
