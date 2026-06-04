package my.project.bookingservice.pricing.service;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.cache.PricingOfferCacheDto;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.context.PricingContextFactory;
import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.response.PricingOfferResponse;
import my.project.bookingservice.pricing.mapper.PricingOfferMapper;
import my.project.bookingservice.pricing.offer.PricingOfferService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingService {
	private final PricingValidationService validationService;
	private final PricingContextFactory contextFactory;
	private final PricingOfferService offerService;
	private final PricingOfferMapper offerMapper;

	public PricingOfferResponse createOffer(UUID userId, PricingOfferRequest request) {
		validationService.validate(request);
		PricingContext context = contextFactory.create(userId, request);
		PricingOfferCacheDto offer = offerService.getOrCreateOffer(context);
		return offerMapper.toResponse(offer);
	}
}
