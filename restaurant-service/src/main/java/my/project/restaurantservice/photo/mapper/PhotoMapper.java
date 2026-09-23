package my.project.restaurantservice.photo.mapper;

import my.project.restaurantservice.photo.dto.PhotoConfirmResponse;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.photo.dto.PhotoUploadRequest;
import my.project.restaurantservice.photo.entity.PhotoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PhotoMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "bucket", ignore = true)
	@Mapping(target = "objectKey", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "uploadedAt", ignore = true)
	@Mapping(target = "confirmedAt", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "dish", ignore = true)
	PhotoEntity toEntity(PhotoUploadRequest dto);

	List<PhotoEntity> toEntity(List<PhotoUploadRequest> dto);

	@Mapping(target = "publicUrl", source = "publicUrl")
	PhotoDto toDto(PhotoEntity entity, String publicUrl);

	@Mapping(target = "presignedUrl", source = "presignedUrl")
	@Mapping(target = "publicUrl", source = "publicUrl")
	PhotoConfirmResponse toResponse(PhotoEntity entity, String presignedUrl, String publicUrl);

}
