package my.project.restaurantservice.restaurant.mapper;

import my.project.restaurantservice.restaurant.dto.workinghours.WorkingHoursDto;
import my.project.restaurantservice.restaurant.entity.WorkingHoursEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "restaurant", ignore = true)
	@Mapping(target = "dayOfWeek", ignore = true)
	void updateEntity(@MappingTarget WorkingHoursEntity entity, WorkingHoursDto dto);

	WorkingHoursDto toDto(WorkingHoursEntity entity);


	List<WorkingHoursEntity> toEntity(List<WorkingHoursDto> req);

	List<WorkingHoursDto> toDto(List<WorkingHoursEntity> entities);
}
