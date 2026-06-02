package my.project.bookingservice.pricing.context;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.client.BookingDishDto;
import my.project.bookingservice.dto.client.BookingSnapshotRequest;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.offer.PricingOfferHashService;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.PricingMathUtils;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PricingContextFactory {
	private final RestaurantServiceClient restaurantClient;
	private final BookingRepository bookingRepository;
	private final PricingOfferHashService hashService;
	private final PricingProperties properties;
	private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(BookingStatus.RESERVED);

	public PricingContext create(UUID userId, PricingOfferRequest request) {
		List<PricingPreorderItemRequest> items = request.preorderItems() == null ? List.of() : request.preorderItems();
		Set<UUID> dishIds = items.stream()
				.map(PricingPreorderItemRequest::dishId)
				.collect(Collectors.toSet());

		var snapshot = restaurantClient.bookingSnapshot(
				request.restaurantId(),
				new BookingSnapshotRequest(request.tableId(), dishIds)
		);
		var restaurantData = restaurantClient.bookingPricingData(
				request.restaurantId(),
				request.tableId(),
				request.startAt(),
				request.endAt()
		);

		BigDecimal preorderAmount = calculatePreorderAmount(items, snapshot.dishes());
		String cartHash = hashService.hash(userId, request.restaurantId(), request.tableId(), request.startAt(), request.endAt(), items);
		long occupiedTables = bookingRepository.countOccupiedTables(
				request.restaurantId(),
				ACTIVE_BOOKING_STATUSES,
				request.startAt(),
				request.endAt()
		);

		BigDecimal minPricingCharge = firstNonNull(
				restaurantData.minPricingCharge(),
				properties.getDefaults().getRestaurantMinPricingCharge()
		);
		BigDecimal maxPricingCharge = firstNonNull(
				restaurantData.maxPricingCharge(),
				properties.getDefaults().getRestaurantMaxPricingCharge()
		);
		return new PricingContext(
				userId,
				request.restaurantId(),
				request.tableId(),
				request.startAt(),
				request.endAt(),
				Instant.now(),
				items,
				PricingMathUtils.money(preorderAmount),
				cartHash,
				minPricingCharge,
				maxPricingCharge,
				restaurantData.totalTablesCount(),
				Math.toIntExact(occupiedTables)
		);
	}

	private BigDecimal calculatePreorderAmount(List<PricingPreorderItemRequest> items, List<BookingDishDto> dishes) {
		if (items.isEmpty()) {
			return BigDecimal.ZERO;
		}
		Map<UUID, BookingDishDto> dishById = dishes == null
				? Map.of()
				: dishes.stream().collect(Collectors.toMap(BookingDishDto::id, Function.identity()));
		BigDecimal amount = BigDecimal.ZERO;
		for (PricingPreorderItemRequest item : items) {
			BookingDishDto dish = dishById.get(item.dishId());
			if (dish == null) {
				throw new ValidationException("pricing.dish-not-found", item.dishId());
			}
			BigDecimal price = dish.price();
			amount = amount.add(price.multiply(BigDecimal.valueOf(item.quantity())));
		}
		return amount;
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

