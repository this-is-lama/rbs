package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.CreateDishRequest;
import my.project.restaurantservice.dto.DishResponse;
import my.project.restaurantservice.entity.DishEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DishMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "photos", ignore = true)
	DishEntity toEntity(CreateDishRequest createDishRequest);

	DishResponse toResponse(DishEntity dishEntity);


	List<DishEntity> toEntities(List<CreateDishRequest> createDishRequest);

	List<CreateDishRequest> toDtos(List<DishEntity> dishEntities);
}
