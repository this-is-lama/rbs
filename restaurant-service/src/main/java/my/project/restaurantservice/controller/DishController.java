package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.DishDto;
import my.project.restaurantservice.dto.PhotoMetaDto;
import my.project.restaurantservice.service.DishPhotoService;
import my.project.restaurantservice.service.DishService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("restaurants/{restId}/dishes")
@RequiredArgsConstructor
public class DishController {

	private final DishService dishService;
	private final DishPhotoService dishPhotoService;

	@PostMapping()
	public ResponseEntity<UUID> create(@RequestBody DishDto dto, @PathVariable UUID restId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dishService.save(dto, restId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<DishDto> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(dishService.findById(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		dishService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping(
			value = "/{id}/photos",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<List<UUID>> upload(@PathVariable UUID id,
											 @RequestPart("files") List<MultipartFile> files,
											 @RequestPart("meta") List<PhotoMetaDto> metas) {
		var list = dishPhotoService.saveAll(id, files, metas);
		return ResponseEntity.status(HttpStatus.CREATED).body(list);
	}
}
