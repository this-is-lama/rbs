package my.project.restaurantservice.table.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.table.dto.TableDto;
import my.project.restaurantservice.table.dto.TableLayoutUpdateRequest;
import my.project.restaurantservice.table.service.command.TableCommandService;
import my.project.restaurantservice.table.service.query.TableDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restId}/tables")
@RequiredArgsConstructor
public class TableController {

	private final TableCommandService commandService;
	private final TableDetailsService detailsService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<UUID> create(@PathVariable UUID restId,
									   @Valid @RequestBody TableDto dto,
									   Authentication auth) {
		UUID id = commandService.save(dto, restId, auth);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/all")
	public ResponseEntity<List<UUID>> createAll(@PathVariable UUID restId,
												@RequestBody @Valid List<@Valid TableDto> dtos,
												Authentication auth) {
		List<UUID> ids = commandService.saveAll(dtos, restId, auth);
		return ResponseEntity.status(HttpStatus.CREATED).body(ids);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<TableDto> update(@PathVariable UUID restId, @PathVariable UUID id,
										   @RequestBody @Valid TableDto dto, Authentication auth) {
		return ResponseEntity.ok(commandService.update(restId, id, dto, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping("/layout")
	public ResponseEntity<List<TableDto>> updateLayout(@PathVariable UUID restId,
													   @RequestBody @Valid TableLayoutUpdateRequest req,
													   Authentication auth) {
		return ResponseEntity.ok(commandService.updateLayout(restId, req, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID restId, @PathVariable UUID id,
									   Authentication auth) {
		commandService.delete(restId, id, auth);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<TableDto> findById(@PathVariable UUID restId, @PathVariable UUID id,
											 Authentication auth) {
		return ResponseEntity.ok(detailsService.findById(restId, id, auth));
	}
}