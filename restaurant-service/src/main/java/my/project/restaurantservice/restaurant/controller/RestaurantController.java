package my.project.restaurantservice.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.restaurant.dto.RestaurantCardDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDetailsDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.service.command.RestaurantCommandService;
import my.project.restaurantservice.restaurant.service.query.RestaurantDetailsService;
import my.project.restaurantservice.restaurant.service.query.RestaurantQueryService;
import org.springframework.data.domain.Page;
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

	private final RestaurantCommandService commandService;
	private final RestaurantDetailsService detailsService;
	private final RestaurantQueryService queryService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UUID> create(@Valid @RequestBody RestaurantDto dto,
									   Authentication auth) {
		UUID id = commandService.create(dto, auth);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<RestaurantDto> update(@PathVariable UUID id,
												@RequestBody @Valid RestaurantDto dto,
												Authentication auth) {
		RestaurantDto response = commandService.update(id, dto, auth);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/my")
	public ResponseEntity<Page<RestaurantCardDto>> findMy(@RequestParam(required = false) Boolean active,
														  @RequestParam(required = false) String category,
														  @RequestParam(required = false) String name,
														  @RequestParam(required = false) String address,
														  @RequestParam(defaultValue = "0") int page,
														  @RequestParam(defaultValue = "10") int size,
														  Authentication auth) {
		return ResponseEntity.ok(detailsService.findMy(active, category, name, address, page, size, auth));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantDetailsDto> findById(@PathVariable UUID id, Authentication auth) {
		return ResponseEntity.ok(detailsService.findById(id, auth));
	}

	@GetMapping
	public ResponseEntity<Page<RestaurantCardDto>> findAll(@RequestParam(required = false) String category,
														   @RequestParam(required = false) String name,
														   @RequestParam(required = false) Boolean active,
														   @RequestParam(required = false) String address,
														   @RequestParam(defaultValue = "0") int page,
														   @RequestParam(defaultValue = "10") int size,
														   Authentication auth) {
		return ResponseEntity.ok(detailsService.findAll(category, name, active, address, page, size, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
		commandService.delete(id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/categories")
	public ResponseEntity<List<String>> findAllCategories() {
		return ResponseEntity.ok(queryService.findAllCategories());
	}
}
