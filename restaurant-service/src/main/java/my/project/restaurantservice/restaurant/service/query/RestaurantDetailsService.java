package my.project.restaurantservice.restaurant.service.query;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dish.service.query.DishDetailsService;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.photo.service.query.PhotoQueryService;
import my.project.restaurantservice.restaurant.dto.RestaurantCardDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDetailsDto;
import my.project.restaurantservice.restaurant.entity.RestaurantSpecifications;
import my.project.restaurantservice.restaurant.mapper.RestaurantMapper;
import my.project.restaurantservice.table.service.query.TableDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class RestaurantDetailsService {

	private final RestaurantMapper mapper;

	private final ManagerAccessService managerAccessService;
	private final RestaurantQueryService queryService;

	private final DishDetailsService dishDetailsService;
	private final TableDetailsService tableDetailsService;
	private final PhotoQueryService photoQueryService;

	@Transactional(readOnly = true)
	public RestaurantDetailsDto findById(UUID id, Authentication auth) {
		var restaurant = managerAccessService.onlyPublicAccess(id, auth)
				? queryService.getPublicById(id)
				: queryService.getPrivateById(id);

		var dishes = dishDetailsService.findAllByRestaurantId(id, auth);
		var tables = tableDetailsService.findAllByRestaurantId(id, auth);
		var photos = photoQueryService.getAllByRestaurantId(id);

		return mapper.toDetailsDto(restaurant, dishes, tables, photos);
	}

	public Page<RestaurantCardDto> findAll(String category, String name,
										   Boolean active, String address,
										   int page, int size, Authentication auth) {
		var spec = RestaurantSpecifications.getSpecification(category, name, active, address);

		if (AuthUtil.isUser(auth)) {
			spec = spec.and(RestaurantSpecifications.isActive(true));
		} else if (AuthUtil.isManager(auth)) {
			spec = spec.and(RestaurantSpecifications.isActiveOrOwnedByManager(AuthUtil.id(auth)));
		}

		return queryService.findCards(spec, pageByName(page, size));
	}

	public Page<RestaurantCardDto> findMy(Boolean active,
										  String category, String name, String address,
										  int page, int size,
										  Authentication auth) {
		var spec = RestaurantSpecifications.getSpecification(category, name, active, address);

		if (AuthUtil.isManager(auth)) {
			spec = spec.and(RestaurantSpecifications.ownedByManager(AuthUtil.id(auth)));
		}

		return queryService.findCards(spec, pageByName(page, size));
	}

	private Pageable pageByName(int page, int size) {
		return PageRequest.of(page, size, Sort.by("name").ascending());
	}
}
