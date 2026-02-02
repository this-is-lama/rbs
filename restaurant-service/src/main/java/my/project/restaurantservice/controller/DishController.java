package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.service.DishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restId}")
@RequiredArgsConstructor
public class DishController {

	private final DishService dishService;

	@GetMapping("dishes/{id}")
	public ResponseEntity<DishDto> findById(@PathVariable UUID restId,
											@PathVariable UUID id) {
		return ResponseEntity.ok(dishService.findById(restId, id));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/dishes/{id}")
	public ResponseEntity<DishDto> update(@PathVariable UUID restId, @PathVariable UUID id,
										  @RequestBody @Valid DishDto dto, Authentication auth) {
		return ResponseEntity.ok(dishService.update(restId, id, dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/dishes/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID restId, @PathVariable UUID id,
									   Authentication auth) {
		dishService.delete(restId, id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
