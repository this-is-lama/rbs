package my.project.bookingservice.pricing.mapper;

import my.project.bookingservice.pricing.cache.PricingOfferCacheDto;
import my.project.bookingservice.pricing.dto.response.PricingOfferResponse;
import org.springframework.stereotype.Component;

@Component
public class PricingOfferMapper {
	public PricingOfferResponse toResponse(PricingOfferCacheDto dto) {
		return new PricingOfferResponse(
				dto.offerId(),
				dto.restaurantId(),
				dto.tableId(),
				dto.preorderAmount(),
				dto.pricingCharge(),
				dto.totalAmount(),
				dto.currency(),
				dto.status(),
				dto.calculatedAt(),
				dto.expiresAt()
		);
	}

}
