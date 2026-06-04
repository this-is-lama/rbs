package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.history.PricingHistoryObservationService;
import my.project.bookingservice.pricing.history.model.PricingHistoryObservation;
import my.project.bookingservice.pricing.weights.result.WeightCalculationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingWeightCalculationService {
	private final PricingHistoryObservationService observationService;
	private final PricingWeightRecommendationService recommendationService;
	private final PricingWeightPersistenceService persistenceService;

	@Transactional
	public WeightCalculationResult calculateRecommendedWeights(UUID restaurantId) {
		List<PricingHistoryObservation> restaurantObservations =
				observationService.getRestaurantIntervalObservationsForWeightUpdate(restaurantId);
		if (restaurantObservations.isEmpty()) {
			return new WeightCalculationResult(restaurantId, List.of());
		}

		List<PricingHistoryObservation> tableObservations =
				observationService.getTableIntervalObservationsForWeightUpdate(restaurantId);

		List<WeightCalculationResult.Item> items = new ArrayList<>();
		items.addAll(persistenceService.apply(
				restaurantId,
				recommendationService.recommendBlockWeights(restaurantId, restaurantObservations)
		));
		items.addAll(persistenceService.apply(
				restaurantId,
				recommendationService.recommendLoadParameterWeights(restaurantObservations)
		));
		items.addAll(persistenceService.apply(
				restaurantId,
				recommendationService.recommendHistoricalParameterWeights(restaurantObservations, tableObservations)
		));
		return new WeightCalculationResult(restaurantId, items);
	}
}
