package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.entity.ManagerEntity;
import my.project.restaurantservice.entity.ManagerId;
import my.project.restaurantservice.repository.ManagerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerPersistenceService {

	private final ManagerRepository repository;

	@CacheEvict(cacheNames = "managerAccess", key = "#restId + ':' + #managerId")
	@Transactional
	public void save(UUID restId, UUID managerId) {
		log.info("Сохранение связи менеджера с рестораном, restId={}, managerId={}", restId, managerId);

		ManagerId linkId = new ManagerId(restId, managerId);
		ManagerEntity entity = new ManagerEntity(linkId, null);
		repository.save(entity);
	}

	@Transactional
	public void deleteByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId) {
		repository.deleteByIdRestaurantIdAndIdManagerId(restId, managerId);
	}

	@Transactional(readOnly = true)
	public boolean existsByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId) {
		return repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId);
	}

	@Transactional(readOnly = true)
	public long countByIdManagerId(UUID managerId) {
		return repository.countByIdManagerId(managerId);
	}

	@Transactional(readOnly = true)
	public List<ManagerEntity> findAllByIdRestaurantIdOrderByCreatedAtAsc(UUID restId) {
		return repository.findAllByIdRestaurantIdOrderByCreatedAtAsc(restId);
	}

	@Cacheable(cacheNames = "managerAccess", key = "#restId + ':' + #managerId", sync = true)
	@Transactional(readOnly = true)
	public boolean managerHasAccess(UUID restId, UUID managerId) {
		boolean result = repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId);
		log.debug("Проверка доступа менеджера к ресторану, restId={}, managerId={}, result={}",
				restId, managerId, result);
		return result;
	}
}
