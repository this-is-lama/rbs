package my.project.restaurantservice.service.dish;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.client.BookingDishDto;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.entity.DishEntity;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.mapper.DishMapper;
import my.project.restaurantservice.repository.DishRepository;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.service.manager.ManagerService;
import my.project.restaurantservice.service.photo.PhotoReadService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {

	private final DishRepository repository;
	private final RestaurantRepository restaurantRepository;

	private final DishMapper mapper;

	private final DishReadService readService;
	private final PhotoReadService photoReadService;
	private final ManagerService managerService;

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicDishesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateDishesByRestaurantId", key = "#restId")
	})
	@Transactional
	public UUID save(DishDto dto, UUID restId, Authentication auth) {
		managerService.checkAccess(restId, auth);
		DishEntity dish = mapper.toEntity(dto);

		RestaurantEntity restaurant = restaurantRepository.getReferenceById(restId);
		restaurant.addDish(dish);

		return repository.save(dish).getId();
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicDishesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateDishesByRestaurantId", key = "#restId")
	})
	@Transactional
	public DishDto update(UUID restId, UUID id, DishDto dto, Authentication auth) {
		managerService.checkAccess(restId, auth);
		var dish = repository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
		mapper.updateEntity(dish, dto);
		return mapper.toDto(dish);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "privateDishById", key = "#restId + ':' + #id"),
			@CacheEvict(cacheNames = "publicDishesByRestaurantId", key = "#restId"),
			@CacheEvict(cacheNames = "privateDishesByRestaurantId", key = "#restId")
	})
	@Transactional
	public void delete(UUID restId, UUID id, Authentication auth) {
		managerService.checkAccess(restId, auth);
		repository.deleteByIdAndRestaurantId(id, restId);
	}

	@Transactional(readOnly = true)
	public DishDto findById(UUID restId, UUID id, Authentication auth) {
		var dish = managerService.onlyPublic(restId, auth) ?
				readService.getPublicById(restId, id) :
				readService.getPrivateById(restId, id);
		var photos = photoReadService.getAllByDishId(id);
		return mapper.copyWithPhotos(dish, photos);
	}


	@Transactional(readOnly = true)
	public DishEntity getRef(UUID id) {
		return repository.findWithRestaurantById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<DishDto> findAllByRestaurantId(UUID restId, Authentication auth) {
		var dishes = managerService.onlyPublic(restId, auth)
				? readService.findAllPublicByRestaurantId(restId)
				: readService.findAllPrivateByRestaurantId(restId);
		Set<UUID> dishIds = dishes.stream().map(DishDto::getId).collect(Collectors.toSet());
		var photosByDishIds = photoReadService.findPhotosForDishes(dishIds);
		return dishes.stream()
				.map(d -> mapper.copyWithPhotos(
						d,
						photosByDishIds.getOrDefault(d.getId(), List.of())))
				.toList();
	}


	@Transactional(readOnly = true)
	public List<BookingDishDto> findRestaurantBookingDishes(UUID restId, Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) return List.of();

		var dishes = repository.findAllByRestaurantIdAndAvailableTrueAndIdIn(restId, ids);
		if (dishes.size() != ids.size()) throw new NotFoundException("restaurant.dish.not-found", ids);

		return mapper.toBookingDto(dishes);
	}

}
