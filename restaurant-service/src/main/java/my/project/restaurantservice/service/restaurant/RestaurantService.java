package my.project.restaurantservice.service.restaurant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.common.exception.ValidationException;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.config.RestaurantPricingProperties;
import my.project.restaurantservice.dto.client.BookingSnapshotRequest;
import my.project.restaurantservice.dto.client.BookingSnapshotResponse;
import my.project.restaurantservice.dto.client.RestaurantBookingPricingDataResponse;
import my.project.restaurantservice.dto.client.RestaurantBookingPricingSummaryResponse;
import my.project.restaurantservice.dto.restaurant.RestaurantCardDto;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantPricingSettingsRequest;
import my.project.restaurantservice.dto.restaurant.RestaurantPricingSettingsResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Moscow");

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
	private final RestaurantPricingProperties pricingProperties;

	@Transactional
	public UUID save(RestaurantDto dto, Authentication auth) {
		log.info("Создание ресторана, name={}", dto.getName());

		RestaurantEntity restaurant = mapper.toEntity(dto);
		applyPricingDefaults(restaurant);
		var managerId = AuthUtil.id(auth);

		contactMapper.toEntity(dto.getContacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.getWorkingHours()).forEach(restaurant::addWorkingHours);

		var restId = repository.save(restaurant).getId();

		if (AuthUtil.isManager(auth)) {
			managerService.save(restId, managerId);
			log.info("Текущий менеджер привязан к ресторану, restId={}, managerId={}", restId, managerId);
		}

		log.info("Ресторан успешно создан, restId={}", restId);
		return restId;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id"),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id")
	})
	@Transactional
	public RestaurantDto update(UUID id, RestaurantDto dto, Authentication auth) {
		log.info("Обновление ресторана, restId={}", id);
		managerService.checkAccess(id, auth);

		var restaurant = repository.findById(id)
				.orElseThrow(() -> {
					log.warn("Ресторан не найден для обновления, restId={}", id);
					return new NotFoundException("restaurant.not-found", id);
				});

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

		log.info("Ресторан успешно обновлён, restId={}", id);
		return readService.getPrivateById(id);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id"),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id")
	})
	@Transactional
	public RestaurantDto setActive(UUID id, boolean active, Authentication auth) {
		log.info("Изменение активности ресторана, restId={}, active={}", id, active);
		managerService.checkAccess(id, auth);

		var restaurant = repository.findById(id)
				.orElseThrow(() -> {
					log.warn("Ресторан не найден для смены активности, restId={}", id);
					return new NotFoundException("restaurant.not-found", id);
				});

		restaurant.setActive(active);

		log.info("Активность ресторана успешно изменена, restId={}, active={}", id, active);
		return readService.getPrivateById(id);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id", beforeInvocation = true)
	})
	@Transactional
	public void delete(UUID id, Authentication auth) {
		log.info("Удаление ресторана, restId={}", id);
		managerService.checkAccess(id, auth);
		repository.deleteById(id);
		log.info("Ресторан успешно удалён, restId={}", id);
	}

	@Transactional(readOnly = true)
	public RestaurantDto findById(UUID id, Authentication auth) {
		log.info("Получение ресторана по id, restId={}", id);

		var restaurant = managerService.onlyPublic(id, auth)
				? readService.getPublicById(id)
				: readService.getPrivateById(id);

		var dishes = dishService.findAllByRestaurantId(id, auth);
		var tables = tableService.findAllByRestaurantId(id, auth);
		var photos = photoReadService.getAllByRestaurantId(id);

		log.debug("Ресторан успешно собран со всеми деталями, restId={}", id);
		return mapper.copyWithDetails(restaurant, dishes, tables, photos);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		log.debug("Получение ссылки на ресторан, restId={}", id);
		return repository.getReferenceById(id);
	}

	@Transactional(readOnly = true)
	public Page<RestaurantCardDto> findAll(String category, String name,
										   Boolean active, String address,
										   int page, int size, Authentication auth) {
		log.info("Поиск ресторанов, category={}, name={}, active={}, address={}, page={}, size={}",
				category, name, active, address, page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		var spec = RestaurantSpecifications.getSpecification(category, name, active, address);

		if (AuthUtil.isUser(auth)) {
			spec = spec.and(RestaurantSpecifications.isActive(true));
		} else if (AuthUtil.isManager(auth)) {
			UUID managerId = AuthUtil.id(auth);
			spec = spec.and(RestaurantSpecifications.isActiveOrOwnedByManager(managerId));
		}

		Page<RestaurantEntity> restaurantsPage = repository.findAll(spec, pageable);
		return buildCardsPage(restaurantsPage, pageable);
	}

	@Transactional(readOnly = true)
	public Page<RestaurantCardDto> findMy(Boolean active,
										  String category,
										  String name,
										  String address,
										  int page,
										  int size,
										  Authentication auth) {
		log.info("Поиск ресторанов текущего владельца/менеджера, active={}, category={}, name={}, address={}, page={}, size={}",
				active, category, name, address, page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		var spec = RestaurantSpecifications.getSpecification(category, name, active, address);

		if (AuthUtil.isManager(auth)) {
			spec = spec.and(RestaurantSpecifications.ownedByManager(AuthUtil.id(auth)));
		}

		Page<RestaurantEntity> restaurantsPage = repository.findAll(spec, pageable);
		return buildCardsPage(restaurantsPage, pageable);
	}

	@Transactional(readOnly = true)
	public BookingSnapshotResponse bookingSnapshot(UUID restId, BookingSnapshotRequest req) {
		log.info("Формирование snapshot для бронирования, restId={}, tableId={}", restId, req.tableId());

		var entity = repository.findByIdAndActiveTrue(restId)
				.orElseThrow(() -> {
					log.warn("Активный ресторан не найден для snapshot, restId={}", restId);
					return new NotFoundException("restaurant.not-found", restId);
				});

		var restaurant = mapper.toBookingDto(entity);
		var dishes = dishService.findRestaurantBookingDishes(restId, req.dishes());
		var table = tableService.findRestaurantBookingTable(restId, req.tableId());

		log.info("Snapshot для бронирования успешно сформирован, restId={}", restId);
		return new BookingSnapshotResponse(restaurant, table, dishes);
	}

	@Transactional(readOnly = true)
	public RestaurantPricingSettingsResponse getPricingSettings(UUID id, Authentication auth) {
		log.info("Getting restaurant pricing settings, restId={}", id);
		managerService.checkAccess(id, auth);
		var restaurant = findRestaurantOrThrow(id);
		return toPricingSettingsResponse(restaurant);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id"),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id")
	})
	@Transactional
	public RestaurantPricingSettingsResponse updatePricingSettings(UUID id,
																   RestaurantPricingSettingsRequest request,
																   Authentication auth) {
		log.info("Updating restaurant pricing settings, restId={}", id);
		managerService.checkAccess(id, auth);
		validatePricingSettings(request.minPricingCharge(), request.maxPricingCharge());
		var restaurant = findRestaurantOrThrow(id);
		restaurant.setMinPricingCharge(request.minPricingCharge());
		restaurant.setMaxPricingCharge(request.maxPricingCharge());
		return toPricingSettingsResponse(restaurant);
	}

	@Transactional(readOnly = true)
	public RestaurantBookingPricingDataResponse bookingPricingData(UUID restId, UUID tableId) {
		var restaurant = findRestaurantOrThrow(restId);
		tableService.findRestaurantBookingTable(restId, tableId);
		long totalTables = tableService.countActiveRestaurantTables(restId);
		return new RestaurantBookingPricingDataResponse(
				restId,
				tableId,
				Math.toIntExact(totalTables),
				restaurant.getMinPricingCharge(),
				restaurant.getMaxPricingCharge()
		);
	}

	@Transactional(readOnly = true)
	public RestaurantBookingPricingSummaryResponse bookingPricingSummary(UUID restId) {
		var restaurant = findRestaurantOrThrow(restId);
		long totalTables = tableService.countActiveRestaurantTables(restId);
		return new RestaurantBookingPricingSummaryResponse(
				restId,
				Math.toIntExact(totalTables),
				restaurant.getMinPricingCharge(),
				restaurant.getMaxPricingCharge()
		);
	}

	@Transactional(readOnly = true)
	public List<String> findAllCategories() {
		log.info("Получение списка категорий ресторанов");

		var categories = repository.findDistinctCategories().stream()
				.map(String::trim)
				.filter(category -> !category.isBlank())
				.distinct()
				.toList();

		log.info("Список категорий успешно получен, count={}", categories.size());
		return categories;
	}

	private Page<RestaurantCardDto> buildCardsPage(Page<RestaurantEntity> restaurantsPage, Pageable pageable) {
		WeekDay today = WeekDay.valueOf(LocalDate.now(BUSINESS_ZONE).getDayOfWeek().name());

		Set<UUID> restIds = restaurantsPage.getContent().stream()
				.map(RestaurantEntity::getId)
				.collect(Collectors.toSet());

		if (restIds.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, restaurantsPage.getTotalElements());
		}

		var banners = photoReadService.findBannersForRestaurants(restIds);
		var whs = workingHoursRepository.findTodayWorkingHoursForRestaurants(restIds, today).stream()
				.collect(Collectors.toMap(wh -> wh.getRestaurant().getId(), wh -> wh));

		List<RestaurantCardDto> cards = restaurantsPage.getContent().stream()
				.map(r -> mapper.toCardDto(r, banners.get(r.getId()), whs.get(r.getId())))
				.toList();

		log.info("Поиск ресторанов завершён, найдено элементов: {}", restaurantsPage.getTotalElements());
		return new PageImpl<>(cards, pageable, restaurantsPage.getTotalElements());
	}

	private RestaurantEntity findRestaurantOrThrow(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", id));
	}

	private void applyPricingDefaults(RestaurantEntity restaurant) {
		restaurant.setMinPricingCharge(firstNonNull(
				restaurant.getMinPricingCharge(),
				pricingProperties.getDefaults().getMinPricingCharge()
		));
		restaurant.setMaxPricingCharge(firstNonNull(
				restaurant.getMaxPricingCharge(),
				pricingProperties.getDefaults().getMaxPricingCharge()
		));
		validatePricingSettings(restaurant.getMinPricingCharge(), restaurant.getMaxPricingCharge());
	}

	private RestaurantPricingSettingsResponse toPricingSettingsResponse(RestaurantEntity restaurant) {
		return new RestaurantPricingSettingsResponse(
				restaurant.getMinPricingCharge(),
				restaurant.getMaxPricingCharge()
		);
	}

	private void validatePricingSettings(BigDecimal minPricingCharge, BigDecimal maxPricingCharge) {
		if (minPricingCharge == null || maxPricingCharge == null) {
			throw new ValidationException("restaurant.pricing-settings.required");
		}
		if (minPricingCharge.compareTo(BigDecimal.ZERO) < 0 || maxPricingCharge.compareTo(BigDecimal.ZERO) < 0) {
			throw new ValidationException("restaurant.pricing-settings.negative");
		}
		if (minPricingCharge.compareTo(maxPricingCharge) > 0) {
			throw new ValidationException("restaurant.pricing-settings.invalid-range");
		}
		if (minPricingCharge.compareTo(pricingProperties.getSystemLimits().getMinPricingCharge()) < 0
				|| maxPricingCharge.compareTo(pricingProperties.getSystemLimits().getMaxPricingCharge()) > 0) {
			throw new ValidationException("restaurant.pricing-settings.out-of-system-limits");
		}
	}

	@SafeVarargs
	private final <T> T firstNonNull(T... values) {
		for (T value : values) {
			if (value != null) {
				return value;
			}
		}
		throw new IllegalStateException("At least one fallback value must be non-null");
	}
}
