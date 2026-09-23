package my.project.restaurantservice.dish.service.query;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.dish.dto.DishDto;
import my.project.restaurantservice.dish.mapper.DishMapper;
import my.project.restaurantservice.dish.repository.DishRepositoryService;
import my.project.restaurantservice.internal.dto.BookingDishDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishQueryService {

	private final DishRepositoryService repositoryService;
	private final DishMapper mapper;

	@Cacheable(cacheNames = "publicDishById", key = "#restId + ':' + #id", sync = true)
	public DishDto getPublicById(UUID restId, UUID id) {
		var dishEntity = repositoryService.getByIdAndRestaurantIdAndAvailableTrue(id, restId);
		return mapper.toDto(dishEntity);
	}

	@Cacheable(cacheNames = "privateDishById", key = "#restId + ':' + #id", sync = true)
	public DishDto getPrivateById(UUID restId, UUID id) {
		var dishEntity = repositoryService.getByIdAndRestaurantId(id, restId);
		return mapper.toDto(dishEntity);
	}

	@Cacheable(cacheNames = "publicDishesByRestaurantId", key = "#restId", sync = true)
	public List<DishDto> findAllPublicByRestaurantId(UUID restId) {
		var dishes = repositoryService.findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(restId);
		return mapper.toDto(dishes);
	}

	@Cacheable(cacheNames = "privateDishesByRestaurantId", key = "#restId", sync = true)
	public List<DishDto> findAllPrivateByRestaurantId(UUID restId) {
		var dishes = repositoryService.findAllByRestaurantIdOrderByNameAsc(restId);
		return mapper.toDto(dishes);
	}

	public List<BookingDishDto> findRestaurantBookingDishes(UUID restId, Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		var dishes = repositoryService.getAllByRestaurantIdAndAvailableTrueAndIdIn(restId, ids);
		return mapper.toBookingDto(dishes);
	}

}
