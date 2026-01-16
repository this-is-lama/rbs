package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.CreateRestaurantRequest;
import my.project.restaurantservice.dto.RestaurantResponse;
import my.project.restaurantservice.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;

	@PostMapping(value = "/create")
	public ResponseEntity<UUID> create(@RequestBody CreateRestaurantRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.save(req));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantResponse> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(restaurantService.findById(id));
	}


}
