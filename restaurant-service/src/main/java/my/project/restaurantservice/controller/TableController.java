package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.table.TableDto;
import my.project.restaurantservice.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restId}/tables")
@RequiredArgsConstructor
public class TableController {

	private final TableService tableService;

	@GetMapping("/{id}")
	public ResponseEntity<TableDto> findById(@PathVariable UUID restId, @PathVariable UUID id) {
		return ResponseEntity.ok(tableService.findById(restId, id));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<TableDto> update(@PathVariable UUID restId, @PathVariable UUID id,
										   @RequestBody @Valid TableDto dto, Authentication auth) {
		return ResponseEntity.ok(tableService.update(restId, id, dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID restId, @PathVariable UUID id,
									   Authentication auth) {
		tableService.delete(restId, id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/{id}/check-table")
	public ResponseEntity<TableDto> checkTable(@PathVariable UUID restId, @PathVariable UUID id) {
		return ResponseEntity.ok(tableService.checkTable(restId, id));
	}

}

