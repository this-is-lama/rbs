package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.DishDto;
import my.project.restaurantservice.dto.RestaurantDto;
import my.project.restaurantservice.dto.RestaurantInfoDto;
import my.project.restaurantservice.dto.TableDto;
import my.project.restaurantservice.service.DishService;
import my.project.restaurantservice.service.RestaurantService;
import my.project.restaurantservice.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;
	private final DishService dishService;
	private final TableService tableService;

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



	@PostMapping("/{restId}/dishes")
	public ResponseEntity<UUID> create(@RequestBody DishDto dto, @PathVariable UUID restId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dishService.save(dto, restId));
	}

	@PostMapping("/{restId}/tables")
	public ResponseEntity<UUID> create(@RequestBody TableDto dto, @PathVariable UUID restId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tableService.save(dto, restId));
	}

}
