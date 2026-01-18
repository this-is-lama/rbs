package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.DishDto;
import my.project.restaurantservice.service.DishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("restaurants/{restId}/dishes")
@RequiredArgsConstructor
public class DishController {

	private final DishService dishService;

	@PostMapping()
	public ResponseEntity<UUID> create(@RequestBody DishDto dto, @PathVariable UUID restId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dishService.save(dto, restId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<DishDto> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(dishService.findById(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<DishDto> delete(@PathVariable UUID id) {
		dishService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
