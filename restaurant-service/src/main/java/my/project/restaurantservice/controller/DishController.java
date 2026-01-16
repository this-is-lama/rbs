package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.CreateDishRequest;
import my.project.restaurantservice.dto.DishResponse;
import my.project.restaurantservice.service.DishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("restaurant/{restId}/dish")
@RequiredArgsConstructor
public class DishController {

	private final DishService dishService;

	@PostMapping("/create")
	public ResponseEntity<UUID> create(@RequestBody CreateDishRequest req, @PathVariable UUID restId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dishService.save(req, restId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<DishResponse> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(dishService.findById(id));
	}
}
