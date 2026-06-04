package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.client.BookingDishDto;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.DishEntity;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.mapper.DishMapper;
import my.project.bookingservice.mapper.RestaurantMapper;
import my.project.bookingservice.mapper.TableMapper;
import my.project.bookingservice.pricing.history.PricingHistoryAggregateCacheEvictService;
import my.project.bookingservice.pricing.offer.PricingOfferUsageService;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ValidationException;
import my.project.common.security.AuthUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCreationService {
	private final BookingRepository bookingRepository;
	private final RestaurantServiceClient restaurantClient;
	private final BookingMapper bookingMapper;
	private final RestaurantMapper restaurantMapper;
	private final TableMapper tableMapper;
	private final DishMapper dishMapper;
	private final BookingPricingApplicationService pricingApplicationService;
	private final PricingOfferUsageService pricingOfferUsageService;
	private final BookingAvailabilityCacheService availabilityCacheService;
	private final PricingHistoryAggregateCacheEvictService historyAggregateCacheEvictService;
	private final RestaurantBookingCacheEvictService restaurantBookingCacheEvictService;
	private final BookingEventService eventService;

	public BookingResponse create(CreateBookingRequest request, Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Создание бронирования, userId={}, restaurantId={}, tableId={}",
				userId, request.restaurantId(), request.tableId());

		BookingSnapshotResponse snapshot = loadSnapshot(request);
		validateSnapshot(snapshot, request);

		BookingEntity booking = buildBooking(request, userId, snapshot);
		pricingApplicationService.apply(request, userId, booking, snapshot);

		BookingEntity saved = save(booking);
		afterSuccessfulCreate(saved, auth);

		log.info("Бронирование успешно создано, bookingId={}, userId={}", saved.getId(), userId);
		return bookingMapper.toResponse(saved);
	}

	private BookingSnapshotResponse loadSnapshot(CreateBookingRequest request) {
		return restaurantClient.bookingSnapshot(
				request.restaurantId(),
				new BookingSnapshotRequest(request.tableId(), request.dishesQuantities().keySet())
		);
	}

	private void validateSnapshot(BookingSnapshotResponse snapshot, CreateBookingRequest request) {
		if (snapshot == null) {
			log.warn("Не удалось получить снимок данных для бронирования, restaurantId={}, tableId={}",
					request.restaurantId(), request.tableId());
			throw new ConflictException("booking.snapshot.not-found");
		}

		if (snapshot.restaurant() == null) {
			log.warn("В снимке бронирования отсутствуют данные ресторана, restaurantId={}", request.restaurantId());
			throw new ConflictException("booking.restaurant.snapshot-not-found");
		}

		if (snapshot.table() == null) {
			log.warn("В снимке бронирования отсутствуют данные стола, restaurantId={}, tableId={}",
					request.restaurantId(), request.tableId());
			throw new ConflictException("booking.table.not-found");
		}

		if (snapshot.table().capacity() < request.guests()) {
			log.warn("Количество гостей превышает вместимость стола, restaurantId={}, tableId={}, guests={}, capacity={}",
					request.restaurantId(), request.tableId(), request.guests(), snapshot.table().capacity());
			throw new ConflictException("booking.table.guest-more-capacity");
		}

		validateDishesSnapshot(snapshot, request);
	}

	private void validateDishesSnapshot(BookingSnapshotResponse snapshot, CreateBookingRequest request) {
		Set<UUID> requestedDishIds = request.dishesQuantities().keySet();
		if (requestedDishIds.isEmpty()) {
			return;
		}

		Set<UUID> returnedDishIds = snapshot.dishes() == null
				? Set.of()
				: snapshot.dishes().stream()
				.map(BookingDishDto::id)
				.collect(Collectors.toSet());

		if (!returnedDishIds.containsAll(requestedDishIds)) {
			log.warn("Список блюд в снимке не соответствует запросу бронирования, restaurantId={}, tableId={}, requested={}, returned={}",
					request.restaurantId(), request.tableId(), requestedDishIds, returnedDishIds);
			throw new ValidationException("booking.dish.snapshot-mismatch");
		}
	}

	private void validateCapacity(BookingSnapshotResponse snapshot, CreateBookingRequest request) {
		if (snapshot == null || snapshot.table() == null) {
			log.warn("Не удалось получить данные стола для бронирования, restaurantId={}, tableId={}",
					request.restaurantId(), request.tableId());
			throw new ConflictException("booking.table.not-found");
		}

		if (snapshot.table().capacity() < request.guests()) {
			log.warn("Количество гостей превышает вместимость стола, restaurantId={}, tableId={}, guests={}, capacity={}",
					request.restaurantId(), request.tableId(), request.guests(), snapshot.table().capacity());
			throw new ConflictException("booking.table.guest-more-capacity");
		}
	}

	private BookingEntity buildBooking(CreateBookingRequest request, UUID userId, BookingSnapshotResponse snapshot) {
		BookingEntity booking = bookingMapper.toEntity(request, userId);
		booking.setRestaurant(restaurantMapper.toEntity(snapshot.restaurant()));
		booking.setTable(tableMapper.toEntity(snapshot.table()));
		attachDishes(booking, request, snapshot);
		return booking;
	}

	private void attachDishes(BookingEntity booking, CreateBookingRequest request, BookingSnapshotResponse snapshot) {
		Map<UUID, Integer> quantities = request.dishesQuantities();
		if (snapshot.dishes() == null || snapshot.dishes().isEmpty()) {
			return;
		}

		for (BookingDishDto dishDto : snapshot.dishes()) {
			DishEntity dish = dishMapper.toEntity(dishDto);
			dish.setQuantity(quantities.getOrDefault(dish.getDishId(), 1));
			booking.addDish(dish);
		}
	}

	private BookingEntity save(BookingEntity booking) {
		try {
			BookingEntity saved = bookingRepository.saveAndFlush(booking);
			log.info("Бронирование сохранено в базе данных, bookingId={}", saved.getId());
			return saved;
		} catch (DataIntegrityViolationException ex) {
			log.warn("Конфликт при сохранении бронирования, restaurantId={}, tableId={}, startAt={}, endAt={}",
					booking.getRestaurantId(), booking.getTableId(), booking.getStartAt(), booking.getEndAt(), ex);
			throw new ConflictException("booking.overlap");
		}
	}

	private void afterSuccessfulCreate(BookingEntity booking, Authentication auth) {
		deletePricingOfferAfterSuccessfulBooking(booking);
		evictAvailabilityCache(booking);
		evictHistoryAggregateCache(booking);
		restaurantBookingCacheEvictService.evict(booking.getRestaurantId());
		eventService.sendCreated(booking, auth);
	}

	private void deletePricingOfferAfterSuccessfulBooking(BookingEntity booking) {
		if (booking.getPricingOfferId() != null) {
			pricingOfferUsageService.deleteAfterSuccessfulBooking(booking.getPricingOfferId());
		}
	}

	private void evictAvailabilityCache(BookingEntity booking) {
		LocalDate date = BookingTimeUtils.businessDate(booking.getStartAt());
		availabilityCacheService.evict(booking.getRestaurantId(), booking.getTableId(), date);
	}

	private void evictHistoryAggregateCache(BookingEntity booking) {
		historyAggregateCacheEvictService.evict(booking.getRestaurantId());
	}
}
