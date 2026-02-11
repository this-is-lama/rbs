package my.project.restaurantservice.service;

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
import my.project.restaurantservice.mapper.ContactMapper;
import my.project.restaurantservice.mapper.RestaurantMapper;
import my.project.restaurantservice.mapper.WorkingHoursMapper;
import my.project.restaurantservice.repository.ContactRepository;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.repository.WorkingHoursRepository;
import my.project.restaurantservice.service.photo.PhotoService;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private final RestaurantMapper restaurantMapper;
	private final ContactMapper contactMapper;
	private final WorkingHoursMapper workingHoursMapper;

	private final RestaurantRepository repository;
	private final WorkingHoursRepository workingHoursRepository;
	private final ContactRepository contactRepository;

	private final ManagerService managerService;
	private final DishService dishService;
	private final TableService tableService;
	private final PhotoService photoService;

	@Transactional
	public UUID save(RestaurantDto dto, Authentication auth) {
		RestaurantEntity restaurant = restaurantMapper.toEntity(dto);
		var managerId = AuthUtil.id(auth);

		contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);

		var restId = repository.save(restaurant).getId();

		if (AuthUtil.isManager(auth)) {
			managerService.save(restId, managerId);
		}

		return restId;
	}

	@Transactional
	public RestaurantDto update(UUID id, RestaurantDto dto, Authentication auth) {
		managerService.checkAccess(id, auth);

		var restaurant = getById(id, auth);
		restaurantMapper.updateEntity(restaurant, dto);

		for (var c : new ArrayList<>(restaurant.getContacts())) {
			restaurant.removeContact(c);
		}
		for (var wh : new ArrayList<>(restaurant.getWorkingHours())) {
			restaurant.removeWorkingHours(wh);
		}

		repository.flush();

		contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);

		return restaurantMapper.toDto(restaurant);
	}

	@Transactional
	public void delete(UUID id, Authentication auth) {
		managerService.checkAccess(id, auth);
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public RestaurantDto findById(UUID id, Authentication auth) {
		var restaurant = getById(id, auth);

		var wh = workingHoursRepository.findAllByRestaurantId(id);
		var contacts = contactRepository.findAllByRestaurantId(id);

		var dishes = dishService.findAllByRestaurantId(id, auth);
		var tables = tableService.findAllByRestaurantId(id, auth);
		var photos = photoService.getAllByRestaurantId(id);

		return restaurantMapper.toDto(restaurant, wh, contacts, dishes, tables, photos);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getById(UUID id, Authentication auth) {
		var userId = AuthUtil.id(auth);

		Optional<RestaurantEntity> restaurant;
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !managerService.managerHasAccess(id, userId))) {
			restaurant = repository.findByIdAndActiveTrue(id);
		} else {
			restaurant = repository.findById(id);
		}
		return restaurant.orElseThrow(() -> new NotFoundException("restaurant.not-found", id));
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

	@Transactional(readOnly = true)
	public Page<RestaurantCardDto> findAll(String category, String name,
										   Boolean active, String address,
										   int page, int size, Authentication auth) {
		DayOfWeek today = LocalDate.now().getDayOfWeek();

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

		var banners = photoService.findBannersForRestaurants(restIds);
		var whs = workingHoursRepository.findTodayWorkingHoursForRestaurants(restIds, today).stream()
				.collect(Collectors.toMap(wh -> wh.getRestaurant().getId(), wh -> wh));

		List<RestaurantCardDto> cards = restaurantsPage.getContent().stream()
				.map(r -> restaurantMapper.toCardDto(
						r,
						banners.get(r.getId()),
						whs.get(r.getId())))
				.toList();

		return new PageImpl<>(cards, pageable, restaurantsPage.getTotalElements());
	}


	@Transactional(readOnly = true)
	public BookingSnapshotResponse bookingSnapshot(UUID restId, BookingSnapshotRequest req) {
		var dishes = dishService.findRestaurantBookingDishes(restId, req.dishes());
		var table = tableService.findRestaurantBookingTable(restId, req.tableId());
		var entity = repository.findByIdAndActiveTrue(restId)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", restId));
		var restaurant = restaurantMapper.toBookingDto(entity);
		return new BookingSnapshotResponse(restaurant, table, dishes);
	}
}
