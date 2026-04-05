package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.service.table.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants/{restId}/tables")
@RequiredArgsConstructor
public class TableController {

	private final TableService tableService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UUID> create(@PathVariable UUID restId,
									   @Valid @RequestBody TableDto dto,
									   Authentication auth) {
		log.info("Получен запрос на создание стола, restId={}, tableNumber={}", restId, dto.tableNumber());
		UUID id = tableService.save(dto, restId, auth);
		log.info("Стол успешно создан, restId={}, tableId={}", restId, id);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<TableDto> update(@PathVariable UUID restId, @PathVariable UUID id,
										   @RequestBody @Valid TableDto dto, Authentication auth) {
		log.info("Получен запрос на обновление стола, restId={}, tableId={}", restId, id);
		return ResponseEntity.ok(tableService.update(restId, id, dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID restId, @PathVariable UUID id,
									   Authentication auth) {
		log.info("Получен запрос на удаление стола, restId={}, tableId={}", restId, id);
		tableService.delete(restId, id, auth);
		log.info("Стол успешно удалён, restId={}, tableId={}", restId, id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<TableDto> findById(@PathVariable UUID restId, @PathVariable UUID id,
											 Authentication auth) {
		log.info("Получен запрос на получение стола, restId={}, tableId={}", restId, id);
		return ResponseEntity.ok(tableService.findById(restId, id, auth));
	}
}