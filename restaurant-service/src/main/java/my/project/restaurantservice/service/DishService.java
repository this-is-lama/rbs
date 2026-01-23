package my.project.restaurantservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.DishDto;
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

		RestaurantEntity restaurantRef = restaurantService.getRef(restId);
		dish.setRestaurant(restaurantRef);

		return repository.save(dish).getId();
	}

	@Transactional(readOnly = true)
	public DishDto findById(UUID id) {
		var dish = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(id.toString()));
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
