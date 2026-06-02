package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.restaurant.RestaurantActiveUpdateRequest;
import my.project.restaurantservice.dto.restaurant.RestaurantCardDto;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantPricingSettingsRequest;
import my.project.restaurantservice.dto.restaurant.RestaurantPricingSettingsResponse;
import my.project.restaurantservice.service.restaurant.RestaurantService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UUID> create(@Valid @RequestBody RestaurantDto dto,
									   Authentication auth) {
		log.info("Получен запрос на создание ресторана, name={}", dto.getName());
		UUID id = restaurantService.save(dto, auth);
		log.info("Ресторан успешно создан, restId={}", id);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<RestaurantDto> update(@PathVariable UUID id,
												@RequestBody @Valid RestaurantDto dto,
												Authentication auth) {
		log.info("Получен запрос на обновление ресторана, restId={}", id);
		RestaurantDto response = restaurantService.update(id, dto, auth);
		log.info("Ресторан успешно обновлён, restId={}", id);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PatchMapping("/{id}/active")
	public ResponseEntity<RestaurantDto> setActive(@PathVariable UUID id,
												   @RequestBody @Valid RestaurantActiveUpdateRequest req,
												   Authentication auth) {
		log.info("Получен запрос на изменение активности ресторана, restId={}, active={}", id, req.active());
		RestaurantDto response = restaurantService.setActive(id, req.active(), auth);
		log.info("Активность ресторана успешно изменена, restId={}, active={}", id, req.active());
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/{id}/pricing-settings")
	public ResponseEntity<RestaurantPricingSettingsResponse> getPricingSettings(@PathVariable UUID id,
																			   Authentication auth) {
		log.info("Received request to get restaurant pricing settings, restId={}", id);
		return ResponseEntity.ok(restaurantService.getPricingSettings(id, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}/pricing-settings")
	public ResponseEntity<RestaurantPricingSettingsResponse> updatePricingSettings(
			@PathVariable UUID id,
			@RequestBody @Valid RestaurantPricingSettingsRequest request,
			Authentication auth
	) {
		log.info("Received request to update restaurant pricing settings, restId={}", id);
		return ResponseEntity.ok(restaurantService.updatePricingSettings(id, request, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/my")
	public ResponseEntity<Page<RestaurantCardDto>> findMy(@RequestParam(required = false) Boolean active,
														  @RequestParam(required = false) String category,
														  @RequestParam(required = false) String name,
														  @RequestParam(required = false) String address,
														  @RequestParam(defaultValue = "0") int page,
														  @RequestParam(defaultValue = "10") int size,
														  Authentication auth) {
		log.info("Получен запрос на список ресторанов текущего пользователя, active={}, category={}, name={}, address={}, page={}, size={}",
				active, category, name, address, page, size);
		return ResponseEntity.ok(restaurantService.findMy(active, category, name, address, page, size, auth));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantDto> findById(@PathVariable UUID id, Authentication auth) {
		log.info("Получен запрос на получение ресторана, restId={}", id);
		return ResponseEntity.ok(restaurantService.findById(id, auth));
	}

	@GetMapping
	public ResponseEntity<Page<RestaurantCardDto>> findAll(@RequestParam(required = false) String category,
														   @RequestParam(required = false) String name,
														   @RequestParam(required = false) Boolean active,
														   @RequestParam(required = false) String address,
														   @RequestParam(defaultValue = "0") int page,
														   @RequestParam(defaultValue = "10") int size,
														   Authentication auth) {
		log.info("Получен запрос на список ресторанов, category={}, name={}, active={}, address={}, page={}, size={}",
				category, name, active, address, page, size);
		return ResponseEntity.ok(restaurantService.findAll(category, name, active, address, page, size, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
		log.info("Получен запрос на удаление ресторана, restId={}", id);
		restaurantService.delete(id, auth);
		log.info("Ресторан успешно удалён, restId={}", id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/categories")
	public ResponseEntity<List<String>> findAllCategories() {
		return ResponseEntity.ok(restaurantService.findAllCategories());
	}
}
