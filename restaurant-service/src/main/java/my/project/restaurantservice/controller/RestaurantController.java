package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantInfoDto;
import my.project.restaurantservice.dto.restaurant.RestaurantPutDto;
import my.project.restaurantservice.dto.table.TableDto;
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
	public ResponseEntity<UUID> create(@Valid @RequestBody RestaurantDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.save(dto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<RestaurantDto> update(@PathVariable UUID id,
												@RequestBody @Valid RestaurantPutDto dto) {
		return ResponseEntity.ok(restaurantService.update(id, dto));
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
	public ResponseEntity<UUID> create(@PathVariable UUID restId, @Valid @RequestBody DishDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dishService.save(dto, restId));
	}

	@PostMapping("/{restId}/tables")
	public ResponseEntity<UUID> create(@PathVariable UUID restId, @Valid @RequestBody TableDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tableService.save(dto, restId));
	}

}
