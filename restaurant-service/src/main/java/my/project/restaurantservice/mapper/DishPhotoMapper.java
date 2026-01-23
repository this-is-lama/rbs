package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.PhotoDto;
import my.project.restaurantservice.dto.PhotoMetaDto;
import my.project.restaurantservice.entity.DishPhotoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DishPhotoMapper {

	@Mapping(target = "objectKey", ignore = true)
	DishPhotoEntity toEntity(PhotoMetaDto dto);

	@Mapping(target = "category", ignore = true)
	PhotoDto toDto(DishPhotoEntity entity);
}
