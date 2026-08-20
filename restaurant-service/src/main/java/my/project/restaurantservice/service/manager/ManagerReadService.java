package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.client.UserServiceClient;
import my.project.restaurantservice.dto.client.UserDto;
import my.project.restaurantservice.dto.manager.RestaurantManagerDto;
import my.project.restaurantservice.entity.ManagerEntity;
import my.project.restaurantservice.mapper.ManagerMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerReadService {

	private final ManagerPersistenceService persistenceService;
	private final ManagerAccessService accessService;

	private final ManagerMapper mapper;

	private final UserServiceClient userClient;

	@Transactional(readOnly = true)
	public List<RestaurantManagerDto> findAllManagersByRestaurantId(UUID restId, Authentication auth) {
		log.info("Получение списка менеджеров ресторана, restId={}", restId);
		accessService.checkAccess(restId, auth);

		List<ManagerEntity> managers = persistenceService.findAllByIdRestaurantIdOrderByCreatedAtAsc(restId);
		if (managers.isEmpty()) {
			return List.of();
		}

		Set<UUID> ids = managers.stream()
				.filter(Objects::nonNull)
				.map(m -> m.getId().getManagerId())
				.collect(Collectors.toCollection(LinkedHashSet::new));

		Map<UUID, UserDto> usersById = userClient.getUsersByIds(ids).stream()
				.collect(Collectors.toMap(UserDto::id, Function.identity()));

		return managers.stream()
				.map(manager -> mapper.toDto(
						manager,
						usersById.get(manager.getId().getManagerId())
				))
				.toList();
	}
}
