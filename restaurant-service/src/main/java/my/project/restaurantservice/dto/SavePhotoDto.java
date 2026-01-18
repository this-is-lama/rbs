package my.project.restaurantservice.dto;

import org.springframework.web.multipart.MultipartFile;

public record SavePhotoDto(
		MultipartFile file,
        boolean isMain,
        Integer sortOrder
) {}
