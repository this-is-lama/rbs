package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.photo.PhotoResponse;
import my.project.restaurantservice.dto.photo.PhotoUploadRequest;
import my.project.restaurantservice.entity.PhotoEntity;
import my.project.restaurantservice.util.PhotoUrlService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class PhotoMapper {

	@Autowired
	protected PhotoUrlService urlService;

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "bucket", ignore = true)
	@Mapping(target = "objectKey", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "uploadedAt", ignore = true)
	@Mapping(target = "confirmedAt", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "dish", ignore = true)
	public abstract PhotoEntity toEntity(PhotoUploadRequest dto);

	@Mapping(target = "presignedUrl", ignore = true)
	public abstract PhotoResponse toDto(PhotoEntity entity);

	@AfterMapping
	protected void fillPublicUrl(PhotoEntity entity, @MappingTarget PhotoResponse dto) {
		if (entity.getBucket() != null && entity.getObjectKey() != null) {
			dto.setPublicUrl(urlService.buildPublicUrl(entity.getBucket(), entity.getObjectKey()));
		}
	}

	public abstract List<PhotoEntity> toEntity(List<PhotoUploadRequest> dto);

	public abstract List<PhotoResponse> toDto(List<PhotoEntity> entity);
}
