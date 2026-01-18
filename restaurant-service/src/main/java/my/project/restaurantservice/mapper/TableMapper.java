package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.TableDto;
import my.project.restaurantservice.entity.TableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	TableEntity toEntity(TableDto dto);

	TableDto toDto(TableEntity entity);

}
