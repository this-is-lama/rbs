package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ForbiddenException;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.restaurantservice.client.UserServiceClient;
import my.project.restaurantservice.dto.manager.AddManagerRequest;
import my.project.restaurantservice.dto.manager.ChangeRoleRequest;
import my.project.restaurantservice.entity.ManagerEntity;
import my.project.restaurantservice.entity.ManagerId;
import my.project.restaurantservice.repository.ManagerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerService {

	private final ManagerRepository repository;

	private final UserServiceClient userClient;
	private final ManagerAccessReadService readService;


	@Transactional
	public UUID addManagerByEmail(UUID restId, AddManagerRequest req, Authentication auth) {
		var email = req.email();
		checkAccess(restId, auth);
		UUID managerId = userClient.changeRole(new ChangeRoleRequest(email, UserRole.ROLE_MANAGER));
		save(restId, managerId);
		return managerId;
	}

	@CacheEvict(cacheNames = "managerAccess", key = "#restId + ':' + #managerId")
	@Transactional
	public void save(UUID restId, UUID managerId) {
		ManagerId linkId = new ManagerId(restId, managerId);
		ManagerEntity entity = new ManagerEntity(linkId, null);
		repository.save(entity);
	}

	@Transactional(readOnly = true)
	public void checkAccess(UUID restId, Authentication auth) {
		if (managerAccess(restId, auth)) {
			throw new ForbiddenException("common.forbidden");
		}
	}

	public boolean managerHasAccess(UUID restId, UUID managerId) {
		return readService.managerHasAccess(restId, managerId);
	}

	@Transactional(readOnly = true)
	public boolean onlyPublic(UUID restId, Authentication auth) {
		return AuthUtil.isUser(auth) || (managerAccess(restId, auth));
	}

	@Transactional(readOnly = true)
	public boolean managerAccess(UUID restId, Authentication auth) {
		return AuthUtil.isManager(auth) && !readService.managerHasAccess(restId, AuthUtil.id(auth));
	}
}

