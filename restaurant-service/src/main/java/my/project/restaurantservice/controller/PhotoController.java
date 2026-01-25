package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.PhotoDto;
import my.project.restaurantservice.service.photo.OwnerType;
import my.project.restaurantservice.service.photo.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PhotoController {

	private final UploadService uploadService;

	@PostMapping("/{container:restaurants|dishes}/{id}/photos/uploads")
	public ResponseEntity<List<PhotoDto>> pendingUpload(@PathVariable String container,
													   @PathVariable UUID id,
													   @RequestBody List<PhotoDto> request) {
		OwnerType type = OwnerType.fromPath(container);
		var dto = uploadService.pendingUpload(type, id, request);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/{container:restaurants|dishes}/photos/confirm")
	public ResponseEntity<List<UUID>> confirmUpload(@PathVariable String container,
									@RequestBody List<PhotoDto> uploaded) {
		OwnerType type = OwnerType.fromPath(container);
		var id = uploadService.confirmUpload(type, uploaded);
		return ResponseEntity.ok(id);
	}

}
