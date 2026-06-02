package my.project.bookingservice.pricing.service;

import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.exception.PricingValidationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PricingValidationService {
	public void validate(PricingOfferRequest request) {
		if (request == null) throw new PricingValidationException("Pricing request must not be null");
		if (request.restaurantId() == null) throw new PricingValidationException("restaurantId must not be null");
		if (request.tableId() == null) throw new PricingValidationException("tableId must not be null");
		if (request.startAt() == null) throw new PricingValidationException("startAt must not be null");
		if (request.endAt() == null) throw new PricingValidationException("endAt must not be null");
		if (!request.startAt().isAfter(Instant.now())) throw new PricingValidationException("startAt must be in the future");
		if (!request.endAt().isAfter(request.startAt())) throw new PricingValidationException("endAt must be after startAt");
		if (request.preorderItems() != null) {
			for (PricingPreorderItemRequest item : request.preorderItems()) {
				if (item.dishId() == null) throw new PricingValidationException("dishId must not be null");
				if (item.quantity() == null || item.quantity() <= 0) throw new PricingValidationException("quantity must be positive");
			}
		}
	}
}

