package my.project.restaurantservice.photo.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.photo.entity.enums.PhotoCategory;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhotoConfirmResponse {

    UUID id;

    String objectKey;

    String presignedUrl;

    String publicUrl;

    String contentType;

    PhotoCategory category;

    int sortOrder;
}
