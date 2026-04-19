package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.common.security.UserRole;
import my.project.restaurantservice.client.UserServiceClient;
import my.project.restaurantservice.dto.client.UserLookupDto;
import my.project.restaurantservice.dto.manager.ChangeRoleByIdRequest;
import my.project.restaurantservice.dto.manager.RestaurantManagerDto;
import my.project.restaurantservice.entity.ManagerEntity;
import my.project.restaurantservice.entity.ManagerId;
import my.project.restaurantservice.mapper.ManagerMapper;
import my.project.restaurantservice.repository.ManagerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerService {

	private final ManagerRepository repository;
	private final UserServiceClient userClient;
	private final ManagerAccessReadService readService;
	private final ManagerMapper managerMapper;

	@Transactional
	public UUID addManagerById(UUID restId, UUID managerId, Authentication auth) {
		log.info("Добавление менеджера к ресторану, restId={}, managerId={}", restId, managerId);

		checkAccess(restId, auth);

		if (repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId)) {
			log.warn("Менеджер уже привязан к ресторану, restId={}, managerId={}", restId, managerId);
			throw new ConflictException("restaurant.manager.already-assigned");
		}

		userClient.changeRoleById(new ChangeRoleByIdRequest(managerId, UserRole.ROLE_MANAGER));
		save(restId, managerId);

		log.info("Менеджер успешно добавлен, restId={}, managerId={}", restId, managerId);
		return managerId;
	}

	@Transactional(readOnly = true)
	public List<RestaurantManagerDto> findAllByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка менеджеров ресторана, restId={}", restId);
		checkAccess(restId, auth);

		List<ManagerEntity> links = repository.findAllByIdRestaurantIdOrderByCreatedAtAsc(restId);
		if (links.isEmpty()) {
			return List.of();
		}

		Set<UUID> ids = links.stream()
				.map(link -> link.getId().getManagerId())
				.collect(Collectors.toCollection(LinkedHashSet::new));

		Map<UUID, UserLookupDto> usersById = userClient.getSummaries(ids).stream()
				.collect(Collectors.toMap(UserLookupDto::id, Function.identity()));

		return links.stream()
				.map(link -> managerMapper.toRestaurantManagerDto(
						link,
						usersById.get(link.getId().getManagerId())
				))
				.toList();
	}

	@CacheEvict(cacheNames = "managerAccess", key = "#restId + ':' + #managerId")
	@Transactional
	public void save(UUID restId, UUID managerId) {
		log.info("Сохранение связи менеджера с рестораном, restId={}, managerId={}", restId, managerId);

		ManagerId linkId = new ManagerId(restId, managerId);
		ManagerEntity entity = new ManagerEntity(linkId, null);
		repository.save(entity);
	}

	@CacheEvict(cacheNames = "managerAccess", key = "#restId + ':' + #managerId")
	@Transactional
	public void deleteManagerById(UUID restId, UUID managerId, Authentication auth) {
		log.info("Удаление связи менеджера с рестораном, restId={}, managerId={}", restId, managerId);
		checkAccess(restId, auth);

		ManagerId linkId = new ManagerId(restId, managerId);
		if (!repository.existsById(linkId)) {
			log.warn("Связь менеджера с рестораном не найдена, restId={}, managerId={}", restId, managerId);
			throw new NotFoundException("restaurant.manager.not-found", managerId);
		}

		repository.deleteById(linkId);

		long remainingLinks = repository.countByIdManagerId(managerId);
		if (remainingLinks == 0) {
			try {
				userClient.changeRoleById(new ChangeRoleByIdRequest(managerId, UserRole.ROLE_USER));
				log.info("Пользователь переведён обратно в ROLE_USER, managerId={}", managerId);
			} catch (Exception ex) {
				log.warn("Не удалось перевести пользователя обратно в ROLE_USER, managerId={}", managerId, ex);
			}
		}

		log.info("Связь менеджера с рестораном удалена, restId={}, managerId={}", restId, managerId);
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