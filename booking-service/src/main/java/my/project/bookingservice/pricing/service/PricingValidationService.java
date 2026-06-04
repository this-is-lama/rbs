package my.project.bookingservice.pricing.service;

import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.exception.PricingValidationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PricingValidationService {
	public void validate(PricingOfferRequest request) {
		if (request == null) throw new PricingValidationException("pricing.request.required");
		if (request.restaurantId() == null) throw new PricingValidationException("pricing.restaurant.required");
		if (request.tableId() == null) throw new PricingValidationException("pricing.table.required");
		if (request.startAt() == null) throw new PricingValidationException("pricing.start.required");
		if (request.endAt() == null) throw new PricingValidationException("pricing.end.required");
		if (!request.startAt().isAfter(Instant.now())) throw new PricingValidationException("pricing.start.future");
		if (!request.endAt().isAfter(request.startAt())) throw new PricingValidationException("pricing.end.after-start");
		if (request.preorderItems() != null) {
			for (PricingPreorderItemRequest item : request.preorderItems()) {
				if (item.dishId() == null) throw new PricingValidationException("pricing.dish.required");
				if (item.quantity() == null || item.quantity() <= 0) throw new PricingValidationException("pricing.quantity.positive");
			}
		}
	}
}

