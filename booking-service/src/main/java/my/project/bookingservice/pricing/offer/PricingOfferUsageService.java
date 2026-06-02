package my.project.bookingservice.pricing.offer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingOfferRepository;
import my.project.common.exception.ConflictException;
import my.project.common.exception.NotFoundException;
import my.project.common.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingOfferUsageService {
	private final PricingOfferRepository offerRepository;
	private final PricingOfferExpirationService expirationService;

	@Transactional
	public PricingOfferEntity validateAndUse(PricingContext context, UUID offerId) {
		if (offerId == null) {
			throw new ValidationException("pricing.offer.required");
		}

		PricingOfferEntity offer = offerRepository.findById(offerId)
				.orElseThrow(() -> new NotFoundException("pricing.offer.not-found", offerId));

		validateOffer(offer, context);
		offer.setStatus(PricingOfferStatus.USED);
		offer.setUpdatedAt(Instant.now());
		PricingOfferEntity saved = offerRepository.save(offer);

		log.info("Ценовое предложение использовано: offerId={}, restaurantId={}, tableId={}",
				saved.getId(), context.restaurantId(), context.tableId());
		return saved;
	}

	private void validateOffer(PricingOfferEntity offer, PricingContext context) {
		validateStatus(offer);
		validateExpiration(offer);
		validateContextMatch(offer, context);
	}

	private void validateStatus(PricingOfferEntity offer) {
		if (offer.getStatus() != PricingOfferStatus.ACTIVE) {
			throw new ConflictException("pricing.offer.not-active");
		}
	}

	private void validateExpiration(PricingOfferEntity offer) {
		if (!expirationService.isActual(offer.getExpiresAt())) {
			offer.setStatus(PricingOfferStatus.EXPIRED);
			offer.setUpdatedAt(Instant.now());
			offerRepository.save(offer);
			log.warn("Ценовое предложение истекло: offerId={}", offer.getId());
			throw new ConflictException("pricing.offer.expired");
		}
	}

	private void validateContextMatch(PricingOfferEntity offer, PricingContext context) {
		if (!Objects.equals(offer.getUserId(), context.userId())
				|| !Objects.equals(offer.getRestaurantId(), context.restaurantId())
				|| !Objects.equals(offer.getTableId(), context.tableId())
				|| !Objects.equals(offer.getVisitStart(), context.visitStart())
				|| !Objects.equals(offer.getVisitEnd(), context.visitEnd())
				|| !Objects.equals(offer.getCartHash(), context.cartHash())) {
			throw new ValidationException("pricing.offer.mismatch");
		}
	}
}
