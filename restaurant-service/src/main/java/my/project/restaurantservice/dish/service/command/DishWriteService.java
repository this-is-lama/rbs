package my.project.restaurantservice.dish.service.command;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dish.dto.DishDto;
import my.project.restaurantservice.dish.entity.DishEntity;
import my.project.restaurantservice.dish.mapper.DishMapper;
import my.project.restaurantservice.dish.repository.DishRepositoryService;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishWriteService {

	private final DishRepositoryService repositoryService;
	private final RestaurantRepositoryService restaurantRepositoryService;
	private final DishMapper mapper;

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicDishesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateDishesByRestaurantId", key = "#restId")
	})
	@Transactional
	public UUID save(DishDto dto, UUID restId) {
		DishEntity dishEntity = mapper.toEntity(dto);
		RestaurantEntity restaurant = restaurantRepositoryService.getRef(restId);
		restaurant.addDish(dishEntity);
		return repositoryService.save(dishEntity).getId();
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicDishesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateDishesByRestaurantId", key = "#restId")
	})
	@Transactional
	public DishDto update(UUID restId, UUID id, DishDto dto) {
		var dish = repositoryService.getByIdAndRestaurantId(id, restId);
		mapper.updateEntity(dish, dto);
		var updatedDish = repositoryService.save(dish);
		return mapper.toDto(updatedDish);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicDishesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateDishesByRestaurantId", key = "#restId")
	})
	@Transactional
	public void deleteByIdAndRestaurantId(UUID id, UUID restId) {
		repositoryService.deleteByIdAndRestaurantId(id, restId);
	}
}
