package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.client.UserDto;
import my.project.restaurantservice.dto.manager.RestaurantManagerDto;
import my.project.restaurantservice.entity.ManagerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

	@Mapping(target = "id", source = "user.id")
	@Mapping(target = "name", source = "user.name")
	@Mapping(target = "surname", source = "user.surname")
	@Mapping(target = "email", source = "user.email")
	@Mapping(target = "role", source = "user.role")
	@Mapping(target = "enabled", source = "user.enabled")
	@Mapping(target = "assignedAt", source = "manager.createdAt")
	RestaurantManagerDto toDto(ManagerEntity manager, UserDto user);


}