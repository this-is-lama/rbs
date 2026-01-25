package my.project.restaurantservice.dto.photo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.entity.enums.PhotoCategory;

public record PhotoUploadRequest(

        @NotBlank
        @Size(max = 32)
        String contentType,

        @NotNull
        PhotoCategory category,

        @Min(0)
        int sortOrder
) {}
