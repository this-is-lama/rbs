package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.history.model.PricingHistoryObservation;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingWeightRecommendationService {
	private final PricingCorrelationService correlationService;
	private final PricingWeightResolver weightResolver;
	private final PricingProperties properties;

	public Map<PricingWeightCode, BigDecimal> recommendBlockWeights(
			UUID restaurantId,
			List<PricingHistoryObservation> observations
	) {
		Map<PricingWeightCode, BigDecimal> importances = new EnumMap<>(PricingWeightCode.class);
		importances.put(
				PricingWeightCode.LOAD_BLOCK,
				correlationService.positiveCorrelation(observations, observation -> loadBlockValue(restaurantId, observation))
		);
		importances.put(
				PricingWeightCode.HISTORICAL_DEMAND_BLOCK,
				correlationService.positiveCorrelation(observations, observation -> historicalBlockValue(restaurantId, observation))
		);
		importances.put(
				PricingWeightCode.CALENDAR_CONTEXT_BLOCK,
				correlationService.positiveCorrelation(observations, PricingHistoryObservation::calendarStatusValue)
		);
		return normalizeImportances(importances);
	}

	public Map<PricingWeightCode, BigDecimal> recommendLoadParameterWeights(List<PricingHistoryObservation> observations) {
		Map<PricingWeightCode, BigDecimal> importances = new EnumMap<>(PricingWeightCode.class);
		importances.put(
				PricingWeightCode.OCCUPANCY_PARAMETER,
				correlationService.positiveCorrelation(observations, PricingHistoryObservation::occupancyValue)
		);
		importances.put(
				PricingWeightCode.URGENCY_PARAMETER,
				correlationService.positiveCorrelation(observations, PricingHistoryObservation::urgencyValue)
		);
		importances.put(
				PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION,
				correlationService.positiveCorrelation(
						observations,
						observation -> multiply(observation.occupancyValue(), observation.urgencyValue())
				)
		);
		return normalizeImportances(importances);
	}

	public Map<PricingWeightCode, BigDecimal> recommendHistoricalParameterWeights(
			List<PricingHistoryObservation> restaurantObservations,
			List<PricingHistoryObservation> tableObservations
	) {
		Map<PricingWeightCode, BigDecimal> importances = new EnumMap<>(PricingWeightCode.class);
		importances.put(
				PricingWeightCode.WEEKDAY_DEMAND_PARAMETER,
				correlationService.positiveCorrelation(restaurantObservations, PricingHistoryObservation::weekdayDemandValue)
		);
		importances.put(
				PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER,
				correlationService.positiveCorrelation(restaurantObservations, PricingHistoryObservation::timeIntervalDemandValue)
		);
		importances.put(
				PricingWeightCode.TABLE_DEMAND_PARAMETER,
				tableObservations.isEmpty()
						? BigDecimal.ZERO
						: correlationService.positiveCorrelation(tableObservations, PricingHistoryObservation::tableDemandValue)
		);
		return normalizeImportances(importances);
	}

	private Map<PricingWeightCode, BigDecimal> normalizeImportances(Map<PricingWeightCode, BigDecimal> importances) {
		BigDecimal total = importances.values().stream()
				.filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return Map.of();
		}

		Map<PricingWeightCode, BigDecimal> result = new EnumMap<>(PricingWeightCode.class);
		importances.forEach((code, importance) -> {
			BigDecimal safeImportance = importance == null ? BigDecimal.ZERO : importance;
			result.put(code, safeImportance.divide(total, 10, RoundingMode.HALF_UP));
		});
		return result;
	}

	private BigDecimal loadBlockValue(UUID restaurantId, PricingHistoryObservation observation) {
		if (observation.occupancyValue() == null || observation.urgencyValue() == null) {
			return null;
		}
		BigDecimal occupancy = value(observation.occupancyValue());
		BigDecimal urgency = value(observation.urgencyValue());
		BigDecimal interaction = occupancy.multiply(urgency);
		Map<PricingWeightCode, BigDecimal> weights = weightResolver.resolveNormalized(restaurantId, List.of(
				PricingWeightCode.OCCUPANCY_PARAMETER,
				PricingWeightCode.URGENCY_PARAMETER,
				PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION
		));
		return weights.get(PricingWeightCode.OCCUPANCY_PARAMETER).multiply(occupancy)
				.add(weights.get(PricingWeightCode.URGENCY_PARAMETER).multiply(urgency))
				.add(weights.get(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION).multiply(interaction));
	}

	private BigDecimal historicalBlockValue(UUID restaurantId, PricingHistoryObservation observation) {
		BigDecimal weekdayDemand = value(observation.weekdayDemandValue());
		BigDecimal timeIntervalDemand = value(observation.timeIntervalDemandValue());
		BigDecimal tableDemand = observation.tableDemandValue() == null
				? properties.getDefaults().getTableDemand()
				: value(observation.tableDemandValue());
		Map<PricingWeightCode, BigDecimal> weights = weightResolver.resolveNormalized(restaurantId, List.of(
				PricingWeightCode.WEEKDAY_DEMAND_PARAMETER,
				PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER,
				PricingWeightCode.TABLE_DEMAND_PARAMETER
		));
		return weights.get(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER).multiply(weekdayDemand)
				.add(weights.get(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER).multiply(timeIntervalDemand))
				.add(weights.get(PricingWeightCode.TABLE_DEMAND_PARAMETER).multiply(tableDemand));
	}

	private BigDecimal multiply(BigDecimal left, BigDecimal right) {
		return left == null || right == null ? null : left.multiply(right);
	}

	private BigDecimal value(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
