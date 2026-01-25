package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.PhotoDto;
import my.project.restaurantservice.entity.PhotoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

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
	PhotoEntity toEntity(PhotoDto dto);

	PhotoDto toDto(PhotoEntity entity);


	List<PhotoEntity> toEntity(List<PhotoDto> dto);

	List<PhotoDto> toDto(List<PhotoEntity> entity);

}
