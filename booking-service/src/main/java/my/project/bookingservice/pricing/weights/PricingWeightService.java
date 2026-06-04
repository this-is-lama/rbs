package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.persistence.repository.PricingWeightRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingWeightService implements PricingWeightProvider {
	private final PricingWeightRepository repository;
	private final PricingProperties properties;
	private final PricingHistoryService historyService;
	private final HistoricalTransitionService transitionService;

	@Override
	@Cacheable(cacheNames = "pricingWeights", key = "#restaurantId + ':' + #code")
	public BigDecimal getWeight(UUID restaurantId, PricingWeightCode code) {
		return repository.findByRestaurantIdAndWeightCode(restaurantId, code)
				.map(entity -> {
					if (entity.getWeightValue() == null
							|| entity.getWeightValue().compareTo(BigDecimal.ZERO) < 0
							|| entity.getSource() == null
							|| entity.getSource() == PricingValueSource.DEFAULT) {
						return defaultWeight(code);
					}
					if (entity.getSource() == PricingValueSource.MANUAL) {
						return entity.getWeightValue();
					}
					if (entity.getSource() == PricingValueSource.HISTORICAL) {
						return transitionService.blend(
								defaultWeight(code),
								entity.getWeightValue(),
								historyService.countSuccessfulBookings(restaurantId),
								properties.getHistory().getMinBookingsForWeightHistory(),
								properties.getHistory().getFullBookingsForWeightHistory()
						);
					}
					return defaultWeight(code);
				})
				.orElseGet(() -> defaultWeight(code));
	}

	private BigDecimal defaultWeight(PricingWeightCode code) {
		return properties.getDefaults().getWeights().getOrDefault(code.name(), BigDecimal.ZERO);
	}
}

