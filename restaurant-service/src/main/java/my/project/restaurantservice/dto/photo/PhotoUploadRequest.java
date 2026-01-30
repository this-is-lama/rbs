package my.project.restaurantservice.dto.photo;

import jakarta.validation.constraints.*;
import my.project.restaurantservice.entity.enums.PhotoCategory;

public record PhotoUploadRequest(

        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "image/(jpeg|png|webp)", message = "restaurant.storage.unsupported-content-type")
        String contentType,

        @NotNull
        PhotoCategory category,

        @Min(0)
        int sortOrder
) {}
