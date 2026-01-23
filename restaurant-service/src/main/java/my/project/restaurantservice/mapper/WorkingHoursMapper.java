package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.WorkingHoursDto;
import my.project.restaurantservice.entity.WorkingHoursEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkingHoursMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	WorkingHoursEntity toEntity(WorkingHoursDto req);

	List<WorkingHoursEntity> toEntities(List<WorkingHoursDto> req);

	WorkingHoursDto toDto(WorkingHoursEntity entity);

	List<WorkingHoursDto> toDto(List<WorkingHoursEntity> entities);
}
