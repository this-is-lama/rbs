package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.client.BookingDishDto;
import my.project.restaurantservice.dto.dish.DishDto;
import my.project.restaurantservice.entity.DishEntity;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.mapper.DishMapper;
import my.project.restaurantservice.repository.DishRepository;
import my.project.restaurantservice.service.photo.PhotoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishService {

	private final DishRepository repository;

	private final DishMapper dishMapper;

	private final RestaurantService restaurantService;
	private final ManagerService managerService;
	private final PhotoService photoService;

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
		var dish = getById(restId, id, auth);
		dishMapper.updateEntity(dish, dto);
		return dishMapper.toDto(dish);
	}

	@Transactional
	public void delete(UUID restId, UUID id, Authentication auth) {
		managerService.checkAccess(restId, auth);
		repository.deleteByIdAndRestaurantId(id, restId);
	}

	@Transactional(readOnly = true)
	public DishDto findById(UUID restId, UUID id, Authentication auth) {
		var dish = getById(restId, id, auth);
		var photos = photoService.getAllByDishesId(id);
		return dishMapper.toDto(dish, photos);
	}

	@Transactional(readOnly = true)
	public DishEntity getById(UUID restId, UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);

		Optional<DishEntity> dish;
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !managerService.managerHasAccess(restId, userId))) {
			dish = repository.findByIdAndRestaurantIdAndAvailableTrue(id, restId);
		} else {
			dish = repository.findByIdAndRestaurantId(id, restId);
		}
		return dish.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}


	@Transactional(readOnly = true)
	public DishEntity getRef(UUID id) {
		return repository.findWithRestaurantById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.dish.not-found", id));
	}

	@Transactional(readOnly = true)
	public List<DishEntity> findAllByRestaurantId(UUID restId, Authentication auth) {
		var userId = AuthUtil.id(auth);

		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !managerService.managerHasAccess(restId, userId))) {
			return repository.findAllByRestaurantIdAndAvailableTrueOrderByNameAsc(restId);
		}
		return repository.findAllByRestaurantIdOrderByNameAsc(restId);
	}

	@Transactional(readOnly = true)
	public List<BookingDishDto> findRestaurantBookingDishes(UUID restId, Set<UUID> ids) {
		if (ids == null || ids.isEmpty()) return List.of();

		var dishes = repository.findAllByRestaurantIdAndAvailableTrueAndIdIn(restId, ids);
		if (dishes.size() != ids.size()) throw new NotFoundException("restaurant.dish.not-found", ids);

		return dishMapper.toBookingDto(dishes);
	}

}
