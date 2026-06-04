package my.project.bookingservice.pricing.offer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.cache.PricingOfferCacheDto;
import my.project.bookingservice.pricing.cache.PricingOfferCacheService;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingOfferUsageService {
	private final PricingOfferCacheService cacheService;
	private final PricingOfferExpirationService expirationService;

	public PricingOfferCacheDto validateAndUse(PricingContext context, UUID offerId) {
		if (offerId == null) {
			throw new ValidationException("pricing.offer.required");
		}

		PricingOfferCacheDto offer = cacheService.findByOfferId(offerId)
				.orElseThrow(() -> new ConflictException("pricing.offer.expired"));

		validateOffer(offer, context);

		log.info("Ценовое предложение проверено в Redis: offerId={}, restaurantId={}, tableId={}",
				offer.offerId(), context.restaurantId(), context.tableId());
		return offer;
	}

	public void deleteAfterSuccessfulBooking(UUID offerId) {
		cacheService.findByOfferId(offerId).ifPresent(offer -> {
			cacheService.evict(offer);
			log.info("Ценовое предложение удалено из Redis после создания бронирования: offerId={}", offer.offerId());
		});
	}

	private void validateOffer(PricingOfferCacheDto offer, PricingContext context) {
		validateStatus(offer);
		validateExpiration(offer);
		validateContextMatch(offer, context);
	}

	private void validateStatus(PricingOfferCacheDto offer) {
		if (offer.status() != PricingOfferStatus.ACTIVE) {
			throw new ConflictException("pricing.offer.not-active");
		}
	}

	private void validateExpiration(PricingOfferCacheDto offer) {
		if (!expirationService.isActual(offer.expiresAt())) {
			cacheService.evict(offer);
			log.warn("Ценовое предложение истекло и удалено из Redis: offerId={}", offer.offerId());
			throw new ConflictException("pricing.offer.expired");
		}
	}

	private void validateContextMatch(PricingOfferCacheDto offer, PricingContext context) {
		if (!Objects.equals(offer.userId(), context.userId())
				|| !Objects.equals(offer.restaurantId(), context.restaurantId())
				|| !Objects.equals(offer.tableId(), context.tableId())
				|| !Objects.equals(offer.visitStart(), context.visitStart())
				|| !Objects.equals(offer.visitEnd(), context.visitEnd())
				|| !Objects.equals(offer.cartHash(), context.cartHash())) {
			throw new ValidationException("pricing.offer.mismatch");
		}
	}
}
