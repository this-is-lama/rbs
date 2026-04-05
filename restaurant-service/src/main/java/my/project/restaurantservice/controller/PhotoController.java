package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.dto.photo.PhotoConfirmRequest;
import my.project.restaurantservice.dto.photo.PhotoConfirmResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/{container:restaurants|dishes}/{containerId}")
@RequiredArgsConstructor
public class PhotoController {

	private final UploadService uploadService;

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/photos/uploads")
	public ResponseEntity<List<PhotoConfirmResponse>> pendingUpload(@PathVariable String container,
																	@PathVariable UUID containerId,
																	@RequestBody @NotEmpty @Size(max = 32)
																	List<@Valid PhotoUploadRequest> req,
																	Authentication auth) {
		log.info("Получен запрос на подготовку загрузки фото, container={}, containerId={}, count={}",
				container, containerId, req.size());
		ContainerType type = ContainerType.fromPath(container);
		return ResponseEntity.ok(uploadService.pendingUpload(type, containerId, req, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/photos/confirm")
	public ResponseEntity<List<UUID>> confirmUpload(@PathVariable String container,
													@PathVariable UUID containerId,
													@RequestBody @NotEmpty @Size(max = 32)
													List<@Valid PhotoConfirmRequest> uploaded,
													Authentication auth) {
		log.info("Получен запрос на подтверждение загрузки фото, container={}, containerId={}, count={}",
				container, containerId, uploaded.size());
		ContainerType type = ContainerType.fromPath(container);
		return ResponseEntity.ok(uploadService.confirmUpload(type, containerId, uploaded, auth));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@DeleteMapping("/photos/delete")
	public ResponseEntity<Void> delete(@PathVariable String container,
									   @PathVariable UUID containerId,
									   @RequestBody @NotEmpty @Size(max = 200) Set<UUID> ids,
									   Authentication auth) {
		log.info("Получен запрос на удаление фото, container={}, containerId={}, count={}",
				container, containerId, ids.size());
		ContainerType type = ContainerType.fromPath(container);
		uploadService.delete(type, containerId, ids, auth);
		log.info("Фото помечены на удаление, container={}, containerId={}, count={}",
				container, containerId, ids.size());
		return ResponseEntity.accepted().build();
	}
}