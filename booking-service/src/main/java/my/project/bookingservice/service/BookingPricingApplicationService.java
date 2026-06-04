package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.client.BookingSnapshotResponse;
import my.project.bookingservice.dto.request.CreateBookingRequest;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.pricing.cache.PricingOfferCacheDto;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.context.PricingContextFactory;
import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.offer.PricingOfferUsageService;
import my.project.bookingservice.pricing.util.PricingMathUtils;
import my.project.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingPricingApplicationService {
	private final PricingContextFactory pricingContextFactory;
	private final PricingOfferUsageService pricingOfferUsageService;

	public void apply(CreateBookingRequest request, UUID userId, BookingEntity entity) {
		if (!hasPreorder(request)) {
			applyFreeBooking(entity);
			return;
		}

		validatePricingOfferId(request);
		PricingContext context = pricingContextFactory.create(userId, toPricingOfferRequest(request));
		applyOffer(request, entity, context);
	}

	public void apply(CreateBookingRequest request, UUID userId, BookingEntity entity, BookingSnapshotResponse snapshot) {
		if (!hasPreorder(request)) {
			applyFreeBooking(entity);
			return;
		}

		validatePricingOfferId(request);
		PricingContext context = pricingContextFactory.create(userId, toPricingOfferRequest(request), snapshot);
		applyOffer(request, entity, context);
	}

	private void applyFreeBooking(BookingEntity entity) {
		entity.setPricingOfferId(null);
		entity.setPreorderAmount(zeroMoney());
		entity.setPricingCharge(zeroMoney());
		entity.setTotalAmount(zeroMoney());
	}

	private void validatePricingOfferId(CreateBookingRequest request) {
		if (request.pricingOfferId() == null) {
			throw new ValidationException("pricing.offer.required");
		}
	}

	private void applyOffer(CreateBookingRequest request, BookingEntity entity, PricingContext context) {
		PricingOfferCacheDto offer = pricingOfferUsageService.validateAndUse(context, request.pricingOfferId());
		entity.setPricingOfferId(offer.offerId());
		entity.setPreorderAmount(offer.preorderAmount());
		entity.setPricingCharge(offer.pricingCharge());
		entity.setTotalAmount(offer.totalAmount());
	}

	private PricingOfferRequest toPricingOfferRequest(CreateBookingRequest request) {
		List<PricingPreorderItemRequest> preorderItems = request.dishes() == null
				? List.of()
				: request.dishes().stream()
				.map(item -> new PricingPreorderItemRequest(item.dishId(), item.quantity()))
				.toList();

		return new PricingOfferRequest(
				request.restaurantId(),
				request.tableId(),
				request.startAt(),
				request.endAt(),
				preorderItems
		);
	}

	private boolean hasPreorder(CreateBookingRequest request) {
		return request.dishes() != null && !request.dishes().isEmpty();
	}

	private BigDecimal zeroMoney() {
		return PricingMathUtils.money(BigDecimal.ZERO);
	}
}
