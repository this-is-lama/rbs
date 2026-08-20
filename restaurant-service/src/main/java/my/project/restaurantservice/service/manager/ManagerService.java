package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ConflictException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.UserRole;
import my.project.restaurantservice.client.UserServiceClient;
import my.project.restaurantservice.dto.manager.ChangeRoleByIdRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerService {

	private final ManagerPersistenceService persistenceService;
	private final ManagerAccessService accessService;

	private final UserServiceClient userClient;

	@Transactional
	public UUID addManagerById(UUID restId, UUID managerId, Authentication auth) {
		log.info("Добавление менеджера к ресторану, restId={}, managerId={}", restId, managerId);

		accessService.checkAccess(restId, auth);

		if (persistenceService.existsByIdRestaurantIdAndIdManagerId(restId, managerId)) {
			log.warn("Менеджер уже привязан к ресторану, restId={}, managerId={}", restId, managerId);
			throw new ConflictException("restaurant.manager.already-assigned");
		}

		userClient.changeRoleById(new ChangeRoleByIdRequest(managerId, UserRole.ROLE_MANAGER));
		persistenceService.save(restId, managerId);

		log.info("Менеджер успешно добавлен, restId={}, managerId={}", restId, managerId);
		return managerId;
	}


	@CacheEvict(cacheNames = "managerAccess", key = "#restId + ':' + #managerId")
	@Transactional
	public void deleteManagerById(UUID restId, UUID managerId, Authentication auth) {
		log.info("Удаление связи менеджера с рестораном, restId={}, managerId={}", restId, managerId);
		accessService.checkAccess(restId, auth);

		if (!persistenceService.existsByIdRestaurantIdAndIdManagerId(restId, managerId)) {
			log.warn("Связь менеджера с рестораном не найдена, restId={}, managerId={}", restId, managerId);
			throw new NotFoundException("restaurant.manager.not-found", managerId);
		}

		persistenceService.deleteByIdRestaurantIdAndIdManagerId(restId, managerId);

		changeManagerRoleToUser(managerId);

		log.info("Связь менеджера с рестораном удалена, restId={}, managerId={}", restId, managerId);
	}

	private void changeManagerRoleToUser(UUID managerId) {
		long remainingLinks = persistenceService.countByIdManagerId(managerId);
		if (remainingLinks == 0) {
			try {
				userClient.changeRoleById(new ChangeRoleByIdRequest(managerId, UserRole.ROLE_USER));
				log.info("Пользователь переведён обратно в ROLE_USER, managerId={}", managerId);
			} catch (Exception ex) {
				log.warn("Не удалось перевести пользователя обратно в ROLE_USER, managerId={}", managerId, ex);
			}
		}
	}


}