package my.project.restaurantservice.dish.service.query;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.dish.dto.DishDetailsDto;
import my.project.restaurantservice.dish.dto.DishDto;
import my.project.restaurantservice.dish.mapper.DishMapper;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.photo.dto.PhotoDto;
import my.project.restaurantservice.photo.service.query.PhotoQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Loggable
@Service
@RequiredArgsConstructor
public class DishDetailsService {

	private final DishQueryService queryService;
	private final PhotoQueryService photoQueryService;
	private final ManagerAccessService managerAccessService;
	private final DishMapper mapper;

	public DishDetailsDto findById(UUID restId, UUID id, Authentication auth) {
		DishDto dish = managerAccessService.onlyPublicAccess(restId, auth)
				? queryService.getPublicById(restId, id)
				: queryService.getPrivateById(restId, id);

		List<PhotoDto> photos = photoQueryService.getAllByDishId(id);
		return mapper.toDetailsDto(dish, photos);
	}

	public List<DishDetailsDto> findAllByRestaurantId(UUID restId, Authentication auth) {
		List<DishDto> dishes = managerAccessService.onlyPublicAccess(restId, auth)
				? queryService.findAllPublicByRestaurantId(restId)
				: queryService.findAllPrivateByRestaurantId(restId);

		Set<UUID> dishIds = dishes.stream()
				.map(DishDto::getId)
				.collect(Collectors.toSet());
		Map<UUID, List<PhotoDto>> dishPhotos = photoQueryService.findPhotosForDishes(dishIds);

		return dishes.stream()
				.map(d -> mapper.toDetailsDto(d, dishPhotos.getOrDefault(d.getId(), List.of())))
				.toList();
	}


}