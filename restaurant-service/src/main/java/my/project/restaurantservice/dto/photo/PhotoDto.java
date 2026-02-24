package my.project.restaurantservice.dto.photo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import my.project.restaurantservice.entity.enums.PhotoCategory;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhotoDto {

	UUID id;

	String objectKey;

	String publicUrl;

	String contentType;

	PhotoCategory category;

	int sortOrder;
}
