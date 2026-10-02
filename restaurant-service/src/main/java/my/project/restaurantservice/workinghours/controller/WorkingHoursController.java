package my.project.restaurantservice.workinghours.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;
import my.project.restaurantservice.workinghours.dto.WorkingHoursUpdateRequest;
import my.project.restaurantservice.workinghours.service.command.WorkingHoursCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restId}/working-hours")
@RequiredArgsConstructor
public class WorkingHoursController {

	private final WorkingHoursCommandService commandService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping
	public ResponseEntity<List<WorkingHoursDto>> update(@PathVariable UUID restId,
														@RequestBody @Valid WorkingHoursUpdateRequest request,
														Authentication auth) {
		return ResponseEntity.ok(commandService.update(restId, request, auth));
	}
}
