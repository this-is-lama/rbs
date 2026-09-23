package my.project.restaurantservice.dish.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dish.dto.DishDetailsDto;
import my.project.restaurantservice.dish.dto.DishDto;
import my.project.restaurantservice.dish.service.command.DishCommandService;
import my.project.restaurantservice.dish.service.query.DishDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restId}/dishes")
@RequiredArgsConstructor
public class DishController {

	private final DishCommandService commandService;
	private final DishDetailsService detailsService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UUID> create(@PathVariable UUID restId,
									   @Valid @RequestBody DishDto dto,
									   Authentication auth) {
		UUID id = commandService.save(dto, restId, auth);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<DishDto> update(@PathVariable UUID restId, @PathVariable UUID id,
										  @RequestBody @Valid DishDto dto, Authentication auth) {
		return ResponseEntity.ok(commandService.update(restId, id, dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID restId, @PathVariable UUID id,
									   Authentication auth) {
		commandService.delete(restId, id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<DishDetailsDto> findById(@PathVariable UUID restId,
											@PathVariable UUID id,
											Authentication auth) {
		return ResponseEntity.ok(detailsService.findById(restId, id, auth));
	}
}