package my.project.restaurantservice.service.restaurant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.client.BookingSnapshotRequest;
import my.project.restaurantservice.dto.client.BookingSnapshotResponse;
import my.project.restaurantservice.dto.restaurant.RestaurantCardDto;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.entity.RestaurantSpecifications;
import my.project.restaurantservice.entity.enums.WeekDay;
import my.project.restaurantservice.mapper.ContactMapper;
import my.project.restaurantservice.mapper.RestaurantMapper;
import my.project.restaurantservice.mapper.WorkingHoursMapper;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.repository.WorkingHoursRepository;
import my.project.restaurantservice.service.dish.DishService;
import my.project.restaurantservice.service.manager.ManagerService;
import my.project.restaurantservice.service.photo.PhotoReadService;
import my.project.restaurantservice.service.table.TableService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private final RestaurantMapper mapper;
	private final ContactMapper contactMapper;
	private final WorkingHoursMapper workingHoursMapper;

	private final RestaurantRepository repository;
	private final WorkingHoursRepository workingHoursRepository;

	private final ManagerService managerService;
	private final DishService dishService;
	private final TableService tableService;

	private final PhotoReadService photoReadService;
	private final RestaurantReadService readService;

	@Transactional
	public UUID save(RestaurantDto dto, Authentication auth) {
		RestaurantEntity restaurant = mapper.toEntity(dto);
		var managerId = AuthUtil.id(auth);

		contactMapper.toEntity(dto.getContacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.getWorkingHours()).forEach(restaurant::addWorkingHours);

		var restId = repository.save(restaurant).getId();
		if (AuthUtil.isManager(auth)) {
			managerService.save(restId, managerId);
		}

		return restId;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id"),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id")
	})
	@Transactional
	public RestaurantDto update(UUID id, RestaurantDto dto, Authentication auth) {
		managerService.checkAccess(id, auth);

		var restaurant = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", id));

		mapper.updateEntity(restaurant, dto);

		for (var c : new ArrayList<>(restaurant.getContacts())) {
			restaurant.removeContact(c);
		}
		for (var wh : new ArrayList<>(restaurant.getWorkingHours())) {
			restaurant.removeWorkingHours(wh);
		}

		repository.flush();

		contactMapper.toEntity(dto.getContacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.getWorkingHours()).forEach(restaurant::addWorkingHours);

		return mapper.toDto(restaurant);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id", beforeInvocation = true)
	})
	@Transactional
	public void delete(UUID id, Authentication auth) {
		managerService.checkAccess(id, auth);
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public RestaurantDto findById(UUID id, Authentication auth) {
		var restaurant = managerService.onlyPublic(id, auth)
				? readService.getPublicById(id)
				: readService.getPrivateById(id);
		var dishes = dishService.findAllByRestaurantId(id, auth);
		var tables = tableService.findAllByRestaurantId(id, auth);
		var photos = photoReadService.getAllByRestaurantId(id);
		return mapper.copyWithDetails(restaurant, dishes, tables, photos);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

	@Transactional(readOnly = true)
	public Page<RestaurantCardDto> findAll(String category, String name,
										   Boolean active, String address,
										   int page, int size, Authentication auth) {
		WeekDay today = WeekDay.valueOf(LocalDate.now().getDayOfWeek().name());

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		var spec = RestaurantSpecifications.getSpecification(category, name, active, address);

		if (AuthUtil.isUser(auth)) {
			spec = spec.and(RestaurantSpecifications.isActive(true));
		} else if (AuthUtil.isManager(auth)) {
			UUID managerId = AuthUtil.id(auth);
			spec = spec.and(RestaurantSpecifications.isActiveOrOwnedByManager(managerId));
		}

		Page<RestaurantEntity> restaurantsPage = repository.findAll(spec, pageable);

		Set<UUID> restIds = restaurantsPage.getContent().stream()
				.map(RestaurantEntity::getId)
				.collect(Collectors.toSet());

		var banners = photoReadService.findBannersForRestaurants(restIds);
		var whs = workingHoursRepository.findTodayWorkingHoursForRestaurants(restIds, today).stream()
				.collect(Collectors.toMap(wh -> wh.getRestaurant().getId(), wh -> wh));

		List<RestaurantCardDto> cards = restaurantsPage.getContent().stream()
				.map(r -> mapper.toCardDto(
						r,
						banners.get(r.getId()),
						whs.get(r.getId())))
				.toList();

		return new PageImpl<>(cards, pageable, restaurantsPage.getTotalElements());
	}

	@Transactional(readOnly = true)
	public BookingSnapshotResponse bookingSnapshot(UUID restId, BookingSnapshotRequest req) {
		var entity = repository.findByIdAndActiveTrue(restId)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", restId));

		var restaurant = mapper.toBookingDto(entity);
		var dishes = dishService.findRestaurantBookingDishes(restId, req.dishes());
		var table = tableService.findRestaurantBookingTable(restId, req.tableId());

		return new BookingSnapshotResponse(restaurant, table, dishes);
	}
}
