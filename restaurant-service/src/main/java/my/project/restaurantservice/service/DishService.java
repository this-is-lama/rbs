package my.project.restaurantservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dto.CreateDishRequest;
import my.project.restaurantservice.dto.DishResponse;
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

	private final DishRepository dishRepository;

	private final DishMapper dishMapper;

	private final RestaurantService restaurantService;

	@Transactional
	public UUID save(CreateDishRequest req, UUID restId) {
		DishEntity dish = dishMapper.toEntity(req);

		RestaurantEntity restaurantRef = restaurantService.getRef(restId);
		dish.setRestaurant(restaurantRef);

		return dishRepository.save(dish).getId();
	}

	@Transactional(readOnly = true)
	public DishResponse findById(UUID id) {
		var dish = dishRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(id.toString()));
		return dishMapper.toResponse(dish);
	}
}
