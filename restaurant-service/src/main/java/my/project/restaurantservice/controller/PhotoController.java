package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.photo.PhotoConfirmRequest;
import my.project.restaurantservice.dto.photo.PhotoResponse;
import my.project.restaurantservice.dto.photo.PhotoUploadRequest;
import my.project.restaurantservice.service.photo.UploadService;
import my.project.restaurantservice.service.photo.provider.ContainerType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/{container:restaurants|dishes}/{containerId}")
@RequiredArgsConstructor
public class PhotoController {

	private final UploadService uploadService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/photos/uploads")
	public ResponseEntity<List<PhotoResponse>> pendingUpload(@PathVariable String container,
															 @PathVariable UUID containerId,
															 @RequestBody @NotEmpty @Size(max = 32)
															 List<@Valid PhotoUploadRequest> req,
															 Authentication auth) {
		ContainerType type = ContainerType.fromPath(container);
		var dto = uploadService.pendingUpload(type, containerId, req, auth);
		return ResponseEntity.ok(dto);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/photos/confirm")
	public ResponseEntity<List<UUID>> confirmUpload(@PathVariable String container,
													@PathVariable UUID containerId,
													@RequestBody @NotEmpty @Size(max = 32)
													List<@Valid PhotoConfirmRequest> uploaded,
													Authentication auth) {
		ContainerType type = ContainerType.fromPath(container);
		var ids = uploadService.confirmUpload(type, containerId, uploaded, auth);
		return ResponseEntity.ok(ids);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/photos/delete")
	public ResponseEntity<Void> delete(@PathVariable String container,
									   @PathVariable UUID containerId,
									   @RequestBody @NotEmpty @Size(max = 200) Set<UUID> ids,
									   Authentication auth) {
		ContainerType type = ContainerType.fromPath(container);
		uploadService.delete(type, containerId, ids, auth);
		return ResponseEntity.accepted().build();
	}

}
