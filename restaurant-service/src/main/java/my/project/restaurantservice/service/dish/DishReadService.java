package my.project.restaurantservice.service.dish;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.mapper.DishMapper;
import my.project.restaurantservice.repository.DishRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishReadService {

	private final DishRepository repository;
	private final DishMapper mapper;

	@Cacheable(
			cacheNames = "publicDishById",
			key = "#restId + ':' + #id",
			sync = true
	)
	@Transactional(readOnly = true)
	public DishDto getPublicById(UUID restId, UUID id) {
		return repository.findByIdAndRestaurantIdAndAvailableTrue(id, restId)
				.map(mapper::toDto)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Cacheable(
			cacheNames = "privateDishById",
			key = "#restId + ':' + #id",
			sync = true
	)
	@Transactional(readOnly = true)
	public DishDto getPrivateById(UUID restId, UUID id) {
		return repository.findByIdAndRestaurantId(id, restId)
				.map(mapper::toDto)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Cacheable(
			cacheNames = "publicDishesByRestaurantId",
			key = "#restId",
			sync = true
	)
	@Transactional(readOnly = true)
	public List<DishDto> findAllPublicByRestaurantId(UUID restId) {
		var dishes = repository.findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(restId);
		return mapper.toDto(dishes);
	}

	@Cacheable(
			cacheNames = "privateDishesByRestaurantId",
			key = "#restId",
			sync = true
	)
	@Transactional(readOnly = true)
	public List<DishDto> findAllPrivateByRestaurantId(UUID restId) {
		var dishes = repository.findAllByRestaurantIdOrderByNameAsc(restId);
		return mapper.toDto(dishes);
	}

}
