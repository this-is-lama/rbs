package my.project.restaurantservice.manager.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.manager.entity.ManagerEntity;
import my.project.restaurantservice.manager.repository.ManagerRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerQueryService {

	private final ManagerRepository repository;

	@Cacheable(cacheNames = "managerHasAccess", key = "#restId + ':' + #managerId", sync = true)
	public boolean managerHasAccess(UUID restId, UUID managerId) {
		boolean result = repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId);
		log.debug("Проверка доступа менеджера к ресторану, restId={}, managerId={}, result={}", restId, managerId, result);
		return result;
	}

	@Cacheable(cacheNames = "restaurantManagers", key = "#restId", sync = true)
	public List<ManagerEntity> findAllRestaurantManagers(UUID restId) {
		return repository.findAllByIdRestaurantIdOrderByCreatedAtAsc(restId);
	}
	
	public List<UUID> findManagersWithoutRestaurants(Collection<UUID> managerIds) {
		if (managerIds.isEmpty()) {
			return List.of();
		}
		Set<UUID> withRestaurants = repository.findManagerIdsWithRestaurants(managerIds);
		return managerIds.stream()
				.filter(id -> !withRestaurants.contains(id))
				.toList();
	}

}
