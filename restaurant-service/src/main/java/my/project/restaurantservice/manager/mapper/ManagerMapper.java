package my.project.restaurantservice.manager.mapper;

import my.project.restaurantservice.internal.dto.UserDto;
import my.project.restaurantservice.manager.dto.RestaurantManagerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

	@Mapping(target = ".", source = "user")
	@Mapping(target = "assignedAt", source = "assignedAt")
	RestaurantManagerDto toDto(Instant assignedAt, UserDto user);

}