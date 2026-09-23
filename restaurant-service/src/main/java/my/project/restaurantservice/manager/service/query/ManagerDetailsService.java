package my.project.restaurantservice.manager.service.query;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.internal.client.UserServiceClient;
import my.project.restaurantservice.internal.dto.UserDto;
import my.project.restaurantservice.manager.dto.RestaurantManagerDto;
import my.project.restaurantservice.manager.entity.ManagerEntity;
import my.project.restaurantservice.manager.mapper.ManagerMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Loggable
@Service
@RequiredArgsConstructor
public class ManagerDetailsService {

	private final ManagerQueryService queryService;
	private final ManagerAccessService accessService;

	private final ManagerMapper mapper;

	private final UserServiceClient userClient;

	@Transactional(readOnly = true)
	public List<RestaurantManagerDto> findAllManagersByRestaurantId(UUID restId, Authentication auth) {
		accessService.checkAccess(restId, auth);

		List<ManagerEntity> managers = queryService.findAllRestaurantManagers(restId);
		if (managers.isEmpty()) {
			return List.of();
		}

		Map<UUID, UserDto> usersById = getUsersById(managers);

		return buildRestaurantManagersDto(managers, usersById);
	}

	private Map<UUID, UserDto> getUsersById(List<ManagerEntity> managers) {
		Set<UUID> ids = managers.stream()
				.filter(Objects::nonNull)
				.map(m -> m.getId().getManagerId())
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return userClient.getUsersByIds(ids).stream().collect(
						Collectors.toMap(UserDto::id, Function.identity()));
	}

	private List<RestaurantManagerDto> buildRestaurantManagersDto(List<ManagerEntity> managers,
																  Map<UUID, UserDto> usersById) {
		return managers.stream()
				.map(manager ->
					mapper.toDto(
						manager.getCreatedAt(),
						usersById.get(manager.getId().getManagerId())
					)
				)
				.toList();
	}
}
