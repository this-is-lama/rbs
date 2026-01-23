package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.PhotoDto;
import my.project.restaurantservice.dto.PhotoMetaDto;
import my.project.restaurantservice.entity.RestaurantPhotoEntity;
import org.mapstruct.*;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RestaurantPhotoMapper {

	@Mapping(target = "objectKey", ignore = true)
	RestaurantPhotoEntity toEntity(PhotoMetaDto dto);

	PhotoDto toDto(RestaurantPhotoEntity entity);
}
