package my.project.restaurantservice.contact.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.contact.dto.ContactDto;
import my.project.restaurantservice.contact.dto.ContactsUpdateRequest;
import my.project.restaurantservice.contact.service.command.ContactCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restId}/contacts")
@RequiredArgsConstructor
public class ContactController {

	private final ContactCommandService commandService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PutMapping
	public ResponseEntity<List<ContactDto>> update(@PathVariable UUID restId,
												   @RequestBody @Valid ContactsUpdateRequest request,
												   Authentication auth) {
		return ResponseEntity.ok(commandService.update(restId, request, auth));
	}
}
