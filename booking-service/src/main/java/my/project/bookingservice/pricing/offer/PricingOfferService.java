package my.project.bookingservice.pricing.offer;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.calculator.PricingCalculationResult;
import my.project.bookingservice.pricing.calculator.PricingCalculator;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingOfferService {
	private final PricingOfferRepository offerRepository;
	private final PricingOfferExpirationService expirationService;
	private final PricingCalculator calculator;
	private final PricingOfferFactory offerFactory;
	private final PricingOfferUsageService usageService;

	@Transactional
	public PricingOfferEntity getOrCreateOffer(PricingContext context) {
		Optional<PricingOfferEntity> existing = offerRepository
				.findFirstByUserIdAndRestaurantIdAndTableIdAndCartHashAndVisitStartAndVisitEndAndStatusOrderByCreatedAtDesc(
						context.userId(),
						context.restaurantId(),
						context.tableId(),
						context.cartHash(),
						context.visitStart(),
						context.visitEnd(),
						PricingOfferStatus.ACTIVE
				);

		if (existing.isPresent()) {
			PricingOfferEntity offer = existing.get();
			if (expirationService.isActual(offer.getExpiresAt())) {
				return offer;
			}
			offer.setStatus(PricingOfferStatus.EXPIRED);
			offer.setUpdatedAt(Instant.now());
			offerRepository.save(offer);
		}

		return createNewOffer(context);
	}

	@Transactional
	public PricingOfferEntity useOffer(UUID offerId, PricingContext context) {
		return usageService.validateAndUse(context, offerId);
	}

	private PricingOfferEntity createNewOffer(PricingContext context) {
		expirePreviousOfferIfExists(context);
		PricingCalculationResult result = calculator.calculate(context);
		PricingOfferEntity entity = offerFactory.create(context, result);
		return offerRepository.save(entity);
	}

	private void expirePreviousOfferIfExists(PricingContext context) {
		offerRepository.findFirstByUserIdAndRestaurantIdAndTableIdAndCartHashAndVisitStartAndVisitEndAndStatusOrderByCreatedAtDesc(
						context.userId(), context.restaurantId(), context.tableId(), context.cartHash(), context.visitStart(), context.visitEnd(), PricingOfferStatus.ACTIVE)
				.ifPresent(entity -> {
					entity.setStatus(PricingOfferStatus.EXPIRED);
					entity.setUpdatedAt(Instant.now());
					offerRepository.save(entity);
				});
	}
}

