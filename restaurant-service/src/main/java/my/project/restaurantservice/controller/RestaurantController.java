package my.project.restaurantservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.PhotoDto;
import my.project.restaurantservice.dto.PhotoMetaDto;
import my.project.restaurantservice.dto.RestaurantDto;
import my.project.restaurantservice.dto.RestaurantInfoDto;
import my.project.restaurantservice.service.RestaurantPhotoService;
import my.project.restaurantservice.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;
	private final RestaurantPhotoService restaurantPhotoService;

	@PostMapping()
	public ResponseEntity<UUID> create(@RequestBody RestaurantDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.save(dto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantDto> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(restaurantService.findById(id));
	}

	@GetMapping()
	public ResponseEntity<List<RestaurantInfoDto>> findAll() {
		return ResponseEntity.ok(restaurantService.findAll());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		restaurantService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping(
			value = "/{id}/photos",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<List<UUID>> upload(@PathVariable UUID id,
											 @RequestPart("files") List<MultipartFile> files,
											 @RequestPart("meta") List<PhotoMetaDto> metaList) {
		var list = restaurantPhotoService.saveAll(id, files, metaList);
		return ResponseEntity.status(HttpStatus.CREATED).body(list);
	}
}
