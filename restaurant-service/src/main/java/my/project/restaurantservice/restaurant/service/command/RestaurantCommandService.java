package my.project.restaurantservice.restaurant.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.manager.service.command.ManagerCommandService;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.service.query.RestaurantQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class RestaurantCommandService {

	private final RestaurantWriteService writeService;
	private final RestaurantQueryService queryService;
	private final ManagerAccessService managerAccessService;
	private final ManagerCommandService managerCommandService;

	public UUID create(RestaurantDto dto, Authentication auth) {
		UUID managerId = AuthUtil.isManager(auth) ? AuthUtil.id(auth) : null;
		return writeService.save(dto, managerId);
	}

	public RestaurantDto update(UUID id, RestaurantDto dto, Authentication auth) {
		managerAccessService.checkAccess(id, auth);
		writeService.update(id, dto);
		return queryService.getPrivateById(id);
	}

	public void delete(UUID id, Authentication auth) {
		managerAccessService.checkAccess(id, auth);
		List<UUID> managerIds = writeService.deleteById(id);
		managerCommandService.changeRoleToUserIfNoRestaurants(managerIds);
	}

}
