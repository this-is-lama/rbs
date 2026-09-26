package my.project.restaurantservice.manager.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.logging.Loggable;
import my.project.common.security.UserRole;
import my.project.restaurantservice.internal.client.UserGateway;
import my.project.restaurantservice.manager.dto.ChangeRoleByIdRequest;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.manager.service.query.ManagerQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerCommandService {

	private final ManagerWriteService writeService;
	private final ManagerAccessService accessService;
	private final ManagerQueryService queryService;

	private final UserGateway userGateway;

	public UUID addManagerById(UUID restId, UUID managerId, Authentication auth) {
		accessService.checkAccess(restId, auth);
		userGateway.changeRoleById(new ChangeRoleByIdRequest(managerId, UserRole.ROLE_MANAGER));
		writeService.save(restId, managerId);

		return managerId;
	}

	public void deleteManagerById(UUID restId, UUID managerId, Authentication auth) {
		accessService.checkAccess(restId, auth);

		writeService.deleteManagerById(restId, managerId);
		changeRoleToUserIfNoRestaurants(List.of(managerId));
	}
	//TODO: проверить согласованность данных
	public void changeRoleToUserIfNoRestaurants(Collection<UUID> managerIds) {
		for (UUID managerId : queryService.findManagersWithoutRestaurants(managerIds)) {
			try {
				userGateway.changeRoleById(new ChangeRoleByIdRequest(managerId, UserRole.ROLE_USER));
				log.info("Пользователь переведён обратно в ROLE_USER, managerId={}", managerId);
			} catch (Exception ex) {
				log.warn("Не удалось перевести пользователя обратно в ROLE_USER, managerId={}", managerId, ex);
			}
		}
	}


}