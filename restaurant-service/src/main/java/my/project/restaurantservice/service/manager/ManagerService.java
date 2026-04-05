package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerService {

	private final ManagerRepository repository;
	private final UserServiceClient userClient;
	private final ManagerAccessReadService readService;

	@Transactional
	public UUID addManagerByEmail(UUID restId, AddManagerRequest req, Authentication auth) {
		var email = req.email();
		log.info("Добавление менеджера к ресторану, restId={}, email={}", restId, email);

		checkAccess(restId, auth);

		UUID managerId = userClient.changeRole(new ChangeRoleRequest(email, UserRole.ROLE_MANAGER));
		save(restId, managerId);

		log.info("Менеджер успешно добавлен, restId={}, managerId={}", restId, managerId);
		return managerId;
	}

	@CacheEvict(cacheNames = "managerAccess", key = "#restId + ':' + #managerId")
	@Transactional
	public void save(UUID restId, UUID managerId) {
		log.info("Сохранение связи менеджера с рестораном, restId={}, managerId={}", restId, managerId);

		ManagerId linkId = new ManagerId(restId, managerId);
		ManagerEntity entity = new ManagerEntity(linkId, null);
		repository.save(entity);
	}

	@Transactional(readOnly = true)
	public void checkAccess(UUID restId, Authentication auth) {
		if (managerAccess(restId, auth)) {
			log.warn("Доступ к ресторану запрещён, restId={}", restId);
			throw new ForbiddenException("common.forbidden");
		}
	}

	public boolean managerHasAccess(UUID restId, UUID managerId) {
		return readService.managerHasAccess(restId, managerId);
	}

	@Transactional(readOnly = true)
	public boolean onlyPublic(UUID restId, Authentication auth) {
		boolean result = AuthUtil.isUser(auth) || managerAccess(restId, auth);
		log.debug("Определение режима доступа к ресторану, restId={}, onlyPublic={}", restId, result);
		return result;
	}

	@Transactional(readOnly = true)
	public boolean managerAccess(UUID restId, Authentication auth) {
		boolean result = AuthUtil.isManager(auth) && !readService.managerHasAccess(restId, AuthUtil.id(auth));
		log.debug("Проверка доступа менеджера через auth, restId={}, result={}", restId, result);
		return result;
	}
}