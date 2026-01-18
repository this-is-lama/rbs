package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.RestaurantDto;
import my.project.restaurantservice.dto.RestaurantInfoDto;
import my.project.restaurantservice.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;

	@PostMapping()
	public ResponseEntity<UUID> create(@RequestBody RestaurantDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.save(dto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantDto> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(restaurantService.findById(id));
	}

	@GetMapping()
	public ResponseEntity<List<RestaurantInfoDto>> findAll() {
		return ResponseEntity.ok(restaurantService.findAll());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		restaurantService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
