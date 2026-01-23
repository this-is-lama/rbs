package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.DishDto;
import my.project.restaurantservice.dto.TableDto;
import my.project.restaurantservice.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("restaurants/{restId}/tables")
@RequiredArgsConstructor
public class TableController {

	private final TableService tableService;

	@PostMapping()
	public ResponseEntity<UUID> create(@RequestBody TableDto dto, @PathVariable UUID restId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tableService.save(dto, restId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TableDto> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(tableService.findById(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		tableService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}

