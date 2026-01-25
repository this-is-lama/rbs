package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.TableDto;
import my.project.restaurantservice.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/tables")
@RequiredArgsConstructor
public class TableController {

	private final TableService tableService;

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

