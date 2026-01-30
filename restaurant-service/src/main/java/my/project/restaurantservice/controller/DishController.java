package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.service.DishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {

	private final DishService dishService;

	@GetMapping("/{id}")
	public ResponseEntity<DishDto> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(dishService.findById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<DishDto> update(@PathVariable UUID id, @RequestBody @Valid DishDto dto) {
		return ResponseEntity.ok(dishService.update(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		dishService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
