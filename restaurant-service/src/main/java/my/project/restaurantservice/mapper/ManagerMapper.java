package my.project.restaurantservice.mapper;

import my.project.restaurantservice.dto.client.UserLookupDto;
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
	@Mapping(target = "assignedAt", source = "link.createdAt")
	RestaurantManagerDto toDto(ManagerEntity link, UserLookupDto user);

	@Mapping(target = "id", source = "id.managerId")
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "surname", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "role", constant = "ROLE_USER")
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "assignedAt", source = "createdAt")
	RestaurantManagerDto toFallbackDto(ManagerEntity link);

	default RestaurantManagerDto toRestaurantManagerDto(ManagerEntity link, UserLookupDto user) {
		if (user == null) {
			return toFallbackDto(link);
		}
		return toDto(link, user);
	}
}