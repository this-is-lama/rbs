package my.project.restaurantservice.manager.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ConflictException;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.manager.entity.ManagerEntity;
import my.project.restaurantservice.manager.entity.ManagerId;
import my.project.restaurantservice.manager.repository.ManagerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerWriteService {

	private final ManagerRepository repository;


	@Caching(evict = {
			@CacheEvict(cacheNames = "managerHasAccess", key = "#restId + ':' + #managerId", beforeInvocation = true),
			@CacheEvict(cacheNames = "restaurantManagers", key = "#restId", beforeInvocation = true)
	})
	@Transactional
	public void save(UUID restId, UUID managerId) {
		if (repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId)) {
			log.warn("Менеджер уже привязан к ресторану, restId={}, managerId={}", restId, managerId);
			throw new ConflictException("restaurant.manager.already-assigned");
		}

		ManagerId linkId = new ManagerId(restId, managerId);
		ManagerEntity entity = new ManagerEntity(linkId, null);
		repository.save(entity);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "managerHasAccess", key = "#restId + ':' + #managerId", beforeInvocation = true),
			@CacheEvict(cacheNames = "restaurantManagers", key = "#restId", beforeInvocation = true)
	})
	@Transactional
	public void deleteManagerById(UUID restId, UUID managerId) {
		if (!repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId)) {
			log.warn("Связь менеджера с рестораном не найдена, restId={}, managerId={}", restId, managerId);
			throw new NotFoundException("restaurant.manager.not-found", managerId);
		}

		repository.deleteByIdRestaurantIdAndIdManagerId(restId, managerId);
	}
}
