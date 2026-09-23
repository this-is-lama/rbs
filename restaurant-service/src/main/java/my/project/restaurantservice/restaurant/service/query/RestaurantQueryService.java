package my.project.restaurantservice.restaurant.service.query;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.internal.dto.BookingRestaurantDto;
import my.project.restaurantservice.photo.service.query.PhotoQueryService;
import my.project.restaurantservice.restaurant.dto.RestaurantCardDto;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.entity.WeekDay;
import my.project.restaurantservice.restaurant.mapper.RestaurantMapper;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantQueryService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Moscow");

	private final RestaurantRepositoryService repositoryService;
	private final RestaurantMapper mapper;

	private final PhotoQueryService photoQueryService;

	@Cacheable(cacheNames = "publicRestaurantById", key = "#id", sync = true)
	@Transactional(readOnly = true)
	public RestaurantDto getPublicById(UUID id) {
		var restaurant = repositoryService.getByIdAndActiveTrue(id);

		var wh = repositoryService.findAllWorkingHoursByRestaurantId(id);
		var contacts = repositoryService.findAllContactsByRestaurantId(id);
		return mapper.toDto(restaurant, wh, contacts);
	}

	@Cacheable(cacheNames = "privateRestaurantById", key = "#id", sync = true)
	@Transactional(readOnly = true)
	public RestaurantDto getPrivateById(UUID id) {
		var restaurant = repositoryService.getById(id);

		var wh = repositoryService.findAllWorkingHoursByRestaurantId(id);
		var contacts = repositoryService.findAllContactsByRestaurantId(id);
		return mapper.toDto(restaurant, wh, contacts);
	}

	@Transactional(readOnly = true)
	public Page<RestaurantCardDto> findCards(Specification<RestaurantEntity> spec, Pageable pageable) {
		Page<RestaurantEntity> restaurantsPage = repositoryService.findAll(spec, pageable);

		Set<UUID> restIds = restaurantsPage.getContent().stream()
				.map(RestaurantEntity::getId)
				.collect(Collectors.toSet());

		if (restIds.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, restaurantsPage.getTotalElements());
		}

		WeekDay today = WeekDay.valueOf(LocalDate.now(BUSINESS_ZONE).getDayOfWeek().name());

		var banners = photoQueryService.findBannersForRestaurants(restIds);
		var whs = repositoryService.findTodayWorkingHoursForRestaurants(restIds, today).stream()
				.collect(Collectors.toMap(
						wh -> wh.getRestaurant().getId(),
						wh -> wh)
				);

		List<RestaurantCardDto> cards = restaurantsPage.getContent().stream()
				.map(r -> mapper.toCardDto(r, banners.get(r.getId()), whs.get(r.getId())))
				.toList();

		return new PageImpl<>(cards, pageable, restaurantsPage.getTotalElements());
	}

	@Loggable
	@Transactional(readOnly = true)
	public List<String> findAllCategories() {
		return repositoryService.findDistinctCategories().stream()
				.map(String::trim)
				.distinct()
				.toList();
	}

	@Transactional(readOnly = true)
	public BookingRestaurantDto getBookingRestaurant(UUID id) {
		return mapper.toBookingDto(repositoryService.getByIdAndActiveTrue(id));
	}
}
