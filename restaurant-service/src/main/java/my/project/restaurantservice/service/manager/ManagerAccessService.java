package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ForbiddenException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerAccessService {

	private final ManagerPersistenceService persistenceService;

	@Transactional(readOnly = true)
	public void checkAccess(UUID restId, Authentication auth) {
		if (managerAccess(restId, auth)) {
			log.warn("Доступ к ресторану запрещён, restId={}", restId);
			throw new ForbiddenException("common.forbidden");
		}
	}

	@Transactional(readOnly = true)
	public boolean onlyPublic(UUID restId, Authentication auth) {
		boolean result = AuthUtil.isUser(auth) || managerAccess(restId, auth);
		log.debug("Определение режима доступа к ресторану, restId={}, onlyPublic={}", restId, result);
		return result;
	}

	@Transactional(readOnly = true)
	public boolean managerAccess(UUID restId, Authentication auth) {
		boolean result = AuthUtil.isManager(auth) && !persistenceService.managerHasAccess(restId, AuthUtil.id(auth));
		log.debug("Проверка доступа менеджера через auth, restId={}, result={}", restId, result);
		return result;
	}



}