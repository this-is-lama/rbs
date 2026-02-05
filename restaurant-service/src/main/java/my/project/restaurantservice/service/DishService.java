package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.entity.DishEntity;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.mapper.DishMapper;
import my.project.restaurantservice.repository.DishRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishService {

	private final DishRepository repository;
	private final DishMapper dishMapper;
	private final RestaurantService restaurantService;
	private final ManagerService managerService;

	@Transactional
	public UUID save(DishDto dto, UUID restId, Authentication auth) {
		managerService.checkAccess(restId, auth);
		DishEntity dish = dishMapper.toEntity(dto);

		RestaurantEntity restaurant = restaurantService.getRef(restId);
		restaurant.addDish(dish);

		return repository.save(dish).getId();
	}

	@Transactional
	public DishDto update(UUID restId, UUID id, DishDto dto, Authentication auth) {
		managerService.checkAccess(restId, auth);
		DishEntity dish = repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
		dishMapper.updateEntity(dish, dto);
		return dishMapper.toDto(dish);
	}


	@Transactional(readOnly = true)
	public DishDto findById(UUID restId, UUID id) {
		var dish = repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
		return dishMapper.toDto(dish);
	}

	@Transactional(readOnly = true)
	public List<DishDto> findAllByIds(UUID restId, List<UUID> ids) {
		var dishes = repository.findAllByRestaurantIdAndAvailableTrueAndIdIn(restId, ids)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", ids));
		if (dishes.size() != ids.size()) {
			throw new NotFoundException("restaurant.dish.not-found", ids);
		}
		return dishMapper.toDto(dishes);
	}

	@Transactional(readOnly = true)
	public DishEntity getRef(UUID id) {
		return repository.findWithRestaurantById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Transactional
	public void delete(UUID restId, UUID id, Authentication auth) {
		managerService.checkAccess(restId, auth);
		repository.deleteByIdAndRestaurantId(id, restId);
	}
}
