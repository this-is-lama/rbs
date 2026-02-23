package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.manager.ChangeRoleRequest;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.common.exception.ForbiddenException;
import my.project.restaurantservice.client.UserServiceClient;
import my.project.restaurantservice.dto.manager.AddManagerRequest;
import my.project.restaurantservice.entity.ManagerEntity;
import my.project.restaurantservice.entity.ManagerId;
import my.project.restaurantservice.repository.ManagerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerService {

	private final ManagerRepository repository;
	private final UserServiceClient userClient;

	@Transactional
	public UUID addManagerByEmail(UUID restId, AddManagerRequest req, Authentication auth) {
		var email = req.email();
		checkAccess(restId, auth);
		UUID managerId = userClient.changeRole(new ChangeRoleRequest(email, UserRole.ROLE_MANAGER));
		save(restId, managerId);
		return managerId;
	}

	@Transactional
	public void save(UUID restId, UUID managerId) {
		ManagerId linkId = new ManagerId(restId, managerId);
		ManagerEntity entity = new ManagerEntity(linkId, null);
		repository.save(entity);
	}

	@Transactional(readOnly = true)
	public boolean checkAccess(UUID restId, Authentication auth) {
		var managerId = AuthUtil.id(auth);
		if (AuthUtil.isManager(auth) && !managerHasAccess(restId, managerId)) {
			throw new ForbiddenException("common.forbidden");
		}
		return true;
	}

	@Transactional(readOnly = true)
	public boolean managerHasAccess(UUID restId, UUID managerId) {
		return repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId);
	}
}

