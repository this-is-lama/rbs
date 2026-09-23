package my.project.restaurantservice.dish.repository;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dish.entity.DishEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishRepositoryService {

	private final DishRepository repository;

	@Transactional(readOnly = true)
	public DishEntity getByIdAndRestaurantIdAndAvailableTrue(UUID id, UUID restId) {
		return repository.findByIdAndRestaurantIdAndAvailableTrue(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Transactional(readOnly = true)
	public DishEntity getByIdAndRestaurantId(UUID id, UUID restId) {
		return repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<DishEntity> findAllByRestaurantIdOrderByNameAsc(UUID restId) {
		return repository.findAllByRestaurantIdOrderByNameAsc(restId);
	}

	@Transactional(readOnly = true)
	public List<DishEntity> findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(UUID restId) {
		return repository.findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(restId);
	}

	@Transactional(readOnly = true)
	public List<DishEntity> getAllByRestaurantIdAndAvailableTrueAndIdIn(UUID restId, Set<UUID> ids) {
		var dishes = repository.findAllByRestaurantIdAndAvailableTrueAndIdIn(restId, ids);
		if (dishes.size() != ids.size()) {
			throw new NotFoundException("restaurant.dish.not-found", ids);
		}
		return dishes;
	}

	@Transactional(readOnly = true)
	public DishEntity getRef(UUID id) {
		return repository.findWithRestaurantById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Transactional
	public DishEntity save(DishEntity dishEntity) {
		return repository.save(dishEntity);
	}

	@Transactional
	public void deleteByIdAndRestaurantId(UUID id, UUID restId) {
		repository.deleteByIdAndRestaurantId(id, restId);
	}
}
