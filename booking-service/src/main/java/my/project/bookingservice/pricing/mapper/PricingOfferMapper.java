package my.project.bookingservice.pricing.mapper;

import my.project.bookingservice.pricing.dto.response.PricingOfferResponse;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import org.springframework.stereotype.Component;

@Component
public class PricingOfferMapper {
	public PricingOfferResponse toResponse(PricingOfferEntity entity) {
		return new PricingOfferResponse(
				entity.getId(),
				entity.getRestaurantId(),
				entity.getTableId(),
				entity.getPreorderAmount(),
				entity.getPricingCharge(),
				entity.getTotalAmount(),
				entity.getCurrency(),
				entity.getStatus(),
				entity.getCalculatedAt(),
				entity.getExpiresAt()
		);
	}
}

