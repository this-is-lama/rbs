package my.project.restaurantservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.photo.PhotoConfirmRequest;
import my.project.restaurantservice.dto.photo.PhotoResponse;
import my.project.restaurantservice.dto.photo.PhotoUploadRequest;
import my.project.restaurantservice.service.photo.PhotoService;
import my.project.restaurantservice.service.photo.UploadService;
import my.project.restaurantservice.service.photo.provider.OwnerType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PhotoController {

	private final UploadService uploadService;
	private final PhotoService photoService;

	@PostMapping("/{container:restaurants|dishes}/{id}/photos/uploads")
	public ResponseEntity<List<PhotoResponse>> pendingUpload(@PathVariable String container,
															 @PathVariable UUID id,
															 @RequestBody @NotEmpty List<@Valid PhotoUploadRequest> request) {
		OwnerType type = OwnerType.fromPath(container);
		var dto = uploadService.pendingUpload(type, id, request);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/{container:restaurants|dishes}/photos/confirm")
	public ResponseEntity<List<UUID>> confirmUpload(@PathVariable String container,
													@RequestBody @NotEmpty List<@Valid PhotoConfirmRequest> uploaded) {
		OwnerType type = OwnerType.fromPath(container);
		var ids = uploadService.confirmUpload(type, uploaded);
		return ResponseEntity.ok(ids);
	}

	@DeleteMapping("/photos/delete")
	public ResponseEntity<List<UUID>> delete(@RequestBody @NotEmpty List<UUID> ids) {
		photoService.markDeleting(ids);
		return ResponseEntity.accepted().build();
	}

}
