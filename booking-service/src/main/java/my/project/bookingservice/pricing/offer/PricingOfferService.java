package my.project.bookingservice.pricing.offer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.cache.PricingOfferCacheDto;
import my.project.bookingservice.pricing.cache.PricingOfferCacheService;
import my.project.bookingservice.pricing.calculator.PricingCalculationResult;
import my.project.bookingservice.pricing.calculator.PricingCalculator;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingOfferService {
	private final PricingOfferCacheService cacheService;
	private final PricingOfferExpirationService expirationService;
	private final PricingCalculator calculator;
	private final PricingOfferFactory offerFactory;
	private final PricingOfferUsageService usageService;

	public PricingOfferCacheDto getOrCreateOffer(PricingContext context) {
		Optional<PricingOfferCacheDto> existing = cacheService.findByCartHash(context.cartHash());
		if (existing.isPresent()) {
			PricingOfferCacheDto offer = existing.get();
			if (isReusable(offer, context)) {
				log.info("Ценовое предложение найдено в Redis: offerId={}, restaurantId={}, tableId={}",
						offer.offerId(), context.restaurantId(), context.tableId());
				return offer;
			}
			cacheService.evict(offer);
			log.info("Ценовое предложение удалено из Redis перед новым расчётом: offerId={}", offer.offerId());
		}

		return createNewOffer(context);
	}

	public PricingOfferCacheDto useOffer(UUID offerId, PricingContext context) {
		return usageService.validateAndUse(context, offerId);
	}

	private PricingOfferCacheDto createNewOffer(PricingContext context) {
		PricingCalculationResult result = calculator.calculate(context);
		PricingOfferCacheDto offer = offerFactory.createCacheDto(context, result);
		cacheService.save(offer);
		log.info("Новое ценовое предложение сохранено в Redis: offerId={}, restaurantId={}, tableId={}",
				offer.offerId(), context.restaurantId(), context.tableId());
		return offer;
	}

	private boolean isReusable(PricingOfferCacheDto offer, PricingContext context) {
		return offer.status() == PricingOfferStatus.ACTIVE
				&& expirationService.isActual(offer.expiresAt())
				&& Objects.equals(offer.userId(), context.userId())
				&& Objects.equals(offer.restaurantId(), context.restaurantId())
				&& Objects.equals(offer.tableId(), context.tableId())
				&& Objects.equals(offer.visitStart(), context.visitStart())
				&& Objects.equals(offer.visitEnd(), context.visitEnd())
				&& Objects.equals(offer.cartHash(), context.cartHash());
	}
}
