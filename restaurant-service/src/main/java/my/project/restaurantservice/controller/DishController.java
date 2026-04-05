package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.service.dish.DishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants/{restId}/dishes")
@RequiredArgsConstructor
public class DishController {

	private final DishService dishService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UUID> create(@PathVariable UUID restId,
									   @Valid @RequestBody DishDto dto,
									   Authentication auth) {
		log.info("Получен запрос на создание блюда, restId={}, name={}", restId, dto.getName());
		UUID id = dishService.save(dto, restId, auth);
		log.info("Блюдо успешно создано, restId={}, dishId={}", restId, id);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<DishDto> update(@PathVariable UUID restId, @PathVariable UUID id,
										  @RequestBody @Valid DishDto dto, Authentication auth) {
		log.info("Получен запрос на обновление блюда, restId={}, dishId={}", restId, id);
		return ResponseEntity.ok(dishService.update(restId, id, dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID restId, @PathVariable UUID id,
									   Authentication auth) {
		log.info("Получен запрос на удаление блюда, restId={}, dishId={}", restId, id);
		dishService.delete(restId, id, auth);
		log.info("Блюдо успешно удалено, restId={}, dishId={}", restId, id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<DishDto> findById(@PathVariable UUID restId,
											@PathVariable UUID id,
											Authentication auth) {
		log.info("Получен запрос на получение блюда, restId={}, dishId={}", restId, id);
		return ResponseEntity.ok(dishService.findById(restId, id, auth));
	}
}