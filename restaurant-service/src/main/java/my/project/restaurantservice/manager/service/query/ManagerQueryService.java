package my.project.restaurantservice.manager.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.manager.entity.ManagerEntity;
import my.project.restaurantservice.manager.repository.ManagerRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
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
	
	public boolean managerWithoutRestaurants(UUID id) {
		return repository.countByIdManagerId(id) == 0;
	}

}
