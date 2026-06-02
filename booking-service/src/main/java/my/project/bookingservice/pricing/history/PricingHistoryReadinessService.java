package my.project.bookingservice.pricing.history;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingHistoryReadinessService {
	private final PricingHistoryService historyService;
	private final PricingProperties properties;

	public boolean hasEnoughHistoryForWeights(UUID restaurantId) {
		return historyService.countSuccessfulBookings(restaurantId) >= properties.getHistory().getMinBookingsForWeightHistory();
	}
}

