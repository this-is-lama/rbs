package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.entity.DishEntity;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.mapper.DishMapper;
import my.project.restaurantservice.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishService {

	private final DishRepository repository;
	private final DishMapper dishMapper;
	private final RestaurantService restaurantService;

	@Transactional
	public UUID save(DishDto dto, UUID restId) {
		DishEntity dish = dishMapper.toEntity(dto);

		RestaurantEntity restaurant = restaurantService.getRef(restId);
		restaurant.addDish(dish);

		return repository.save(dish).getId();
	}

	@Transactional
	public DishDto update(UUID id, DishDto dto) {
		DishEntity dish = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
		dishMapper.updateEntity(dish, dto);
		repository.save(dish);
		return dishMapper.toDto(dish);
	}


	@Transactional(readOnly = true)
	public DishDto findById(UUID id) {
		var dish = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
		return dishMapper.toDto(dish);
	}

	@Transactional(readOnly = true)
	public DishEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

	@Transactional
	public void delete(UUID id) {
		repository.deleteById(id);
	}
}
