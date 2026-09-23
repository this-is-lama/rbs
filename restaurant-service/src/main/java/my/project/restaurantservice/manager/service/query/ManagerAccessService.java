package my.project.restaurantservice.manager.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ForbiddenException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerAccessService {

	private final ManagerQueryService managerQueryService;

	public void checkAccess(UUID restId, Authentication auth) {
		if (AuthUtil.isManager(auth) && !managerQueryService.managerHasAccess(restId, AuthUtil.id(auth))) {
			log.warn("Доступ к ресторану запрещён, restId={}", restId);
			throw new ForbiddenException("common.forbidden");
		}
	}

	public boolean onlyPublicAccess(UUID restId, Authentication auth) {
		boolean result = AuthUtil.isUser(auth) ||  AuthUtil.isManager(auth) && !managerQueryService.managerHasAccess(restId, AuthUtil.id(auth));
		log.debug("Определение режима доступа к ресторану, restId={}, onlyPublic={}", restId, result);
		return result;
	}

}