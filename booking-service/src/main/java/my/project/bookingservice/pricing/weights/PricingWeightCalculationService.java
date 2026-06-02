package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.history.PricingHistoryObservationService;
import my.project.bookingservice.pricing.history.model.PricingHistoryObservation;
import my.project.bookingservice.pricing.persistence.entity.PricingWeightEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingWeightRepository;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.weights.result.WeightCalculationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PricingWeightCalculationService {
	private final PricingHistoryObservationService observationService;
	private final PricingWeightProvider weightProvider;
	private final PricingWeightUpdater weightUpdater;
	private final PricingWeightRepository weightRepository;
	private final PricingProperties properties;

	@Transactional
	public WeightCalculationResult calculateRecommendedWeights(UUID restaurantId) {
		List<PricingHistoryObservation> restaurantObservations = observationService.getRestaurantIntervalObservationsForWeightUpdate(restaurantId);
		if (restaurantObservations.isEmpty()) {
			return new WeightCalculationResult(restaurantId, List.of());
		}
		List<PricingHistoryObservation> tableObservations = observationService.getTableIntervalObservationsForWeightUpdate(restaurantId);

		List<WeightCalculationResult.Item> items = new ArrayList<>();
		items.addAll(updateBlockWeights(restaurantId, restaurantObservations));
		items.addAll(updateLoadParameterWeights(restaurantId, restaurantObservations));
		items.addAll(updateHistoricalParameterWeights(restaurantId, restaurantObservations, tableObservations));
		return new WeightCalculationResult(restaurantId, items);
	}

	private List<WeightCalculationResult.Item> updateBlockWeights(UUID restaurantId, List<PricingHistoryObservation> observations) {
		Map<PricingWeightCode, BigDecimal> importances = new EnumMap<>(PricingWeightCode.class);
		importances.put(PricingWeightCode.LOAD_BLOCK, positiveCorrelation(observations, observation -> loadBlockValue(restaurantId, observation)));
		importances.put(PricingWeightCode.HISTORICAL_DEMAND_BLOCK, positiveCorrelation(observations, observation -> historicalBlockValue(restaurantId, observation)));
		importances.put(PricingWeightCode.CALENDAR_CONTEXT_BLOCK, positiveCorrelation(observations, PricingHistoryObservation::calendarStatusValue));
		return updateWeightGroup(restaurantId, importances);
	}

	private List<WeightCalculationResult.Item> updateLoadParameterWeights(UUID restaurantId, List<PricingHistoryObservation> observations) {
		Map<PricingWeightCode, BigDecimal> importances = new EnumMap<>(PricingWeightCode.class);
		importances.put(PricingWeightCode.OCCUPANCY_PARAMETER, positiveCorrelation(observations, PricingHistoryObservation::occupancyValue));
		importances.put(PricingWeightCode.URGENCY_PARAMETER, positiveCorrelation(observations, PricingHistoryObservation::urgencyValue));
		importances.put(PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION, positiveCorrelation(observations,
				observation -> multiply(observation.occupancyValue(), observation.urgencyValue())));
		return updateWeightGroup(restaurantId, importances);
	}

	private List<WeightCalculationResult.Item> updateHistoricalParameterWeights(UUID restaurantId,
																			   List<PricingHistoryObservation> restaurantObservations,
																			   List<PricingHistoryObservation> tableObservations) {
		Map<PricingWeightCode, BigDecimal> importances = new EnumMap<>(PricingWeightCode.class);
		importances.put(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER, positiveCorrelation(restaurantObservations, PricingHistoryObservation::weekdayDemandValue));
		importances.put(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER, positiveCorrelation(restaurantObservations, PricingHistoryObservation::timeIntervalDemandValue));
		importances.put(PricingWeightCode.TABLE_DEMAND_PARAMETER, tableObservations.isEmpty()
				? BigDecimal.ZERO
				: positiveCorrelation(tableObservations, PricingHistoryObservation::tableDemandValue));
		return updateWeightGroup(restaurantId, importances);
	}

	private List<WeightCalculationResult.Item> updateWeightGroup(UUID restaurantId, Map<PricingWeightCode, BigDecimal> importances) {
		BigDecimal total = importances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return List.of();
		}

		Map<PricingWeightCode, BigDecimal> updatedWeights = new EnumMap<>(PricingWeightCode.class);
		importances.forEach((code, importance) -> {
			BigDecimal recommended = importance.divide(total, 10, java.math.RoundingMode.HALF_UP);
			BigDecimal current = weightProvider.getWeight(restaurantId, code);
			BigDecimal updated = weightUpdater.smoothUpdate(current, recommended);
			updatedWeights.put(code, updated);
		});

		Map<PricingWeightCode, BigDecimal> normalizedUpdatedWeights = NormalizationUtils.normalizeWeights(updatedWeights);
		List<WeightCalculationResult.Item> items = new ArrayList<>();
		importances.forEach((code, importance) -> {
			BigDecimal recommended = importance.divide(total, 10, java.math.RoundingMode.HALF_UP);
			BigDecimal updated = normalizedUpdatedWeights.getOrDefault(code, BigDecimal.ZERO);
			upsertWeight(restaurantId, code, updated);
			items.add(new WeightCalculationResult.Item(code, recommended));
		});
		return items;
	}

	private void upsertWeight(UUID restaurantId, PricingWeightCode code, BigDecimal value) {
		PricingWeightEntity entity = weightRepository.findByRestaurantIdAndWeightCode(restaurantId, code)
				.orElseGet(PricingWeightEntity::new);
		entity.setRestaurantId(restaurantId);
		entity.setWeightCode(code);
		entity.setWeightValue(value);
		entity.setSource(PricingValueSource.HISTORICAL);
		entity.setUpdatedAt(Instant.now());
		weightRepository.save(entity);
	}

	private BigDecimal positiveCorrelation(List<PricingHistoryObservation> observations,
										   Function<PricingHistoryObservation, BigDecimal> valueExtractor) {
		List<double[]> pairs = observations.stream()
				.map(observation -> new BigDecimal[]{
						valueExtractor.apply(observation),
						observation.realizedDemandValue()
				})
				.filter(pair -> pair[0] != null && pair[1] != null)
				.map(pair -> new double[]{pair[0].doubleValue(), pair[1].doubleValue()})
				.toList();
		double[] x = pairs.stream().mapToDouble(pair -> pair[0]).toArray();
		double[] y = pairs.stream().mapToDouble(pair -> pair[1]).toArray();
		if (x.length < 2) {
			return BigDecimal.ZERO;
		}
		double xAvg = average(x);
		double yAvg = average(y);
		double numerator = 0;
		double xVariance = 0;
		double yVariance = 0;
		for (int i = 0; i < x.length; i++) {
			double dx = x[i] - xAvg;
			double dy = y[i] - yAvg;
			numerator += dx * dy;
			xVariance += dx * dx;
			yVariance += dy * dy;
		}
		if (xVariance == 0 || yVariance == 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(Math.max(0, numerator / Math.sqrt(xVariance * yVariance)));
	}

	private BigDecimal loadBlockValue(UUID restaurantId, PricingHistoryObservation observation) {
		if (observation.occupancyValue() == null || observation.urgencyValue() == null) {
			return null;
		}
		BigDecimal occupancy = value(observation.occupancyValue());
		BigDecimal urgency = value(observation.urgencyValue());
		BigDecimal interaction = occupancy.multiply(urgency);
		Map<PricingWeightCode, BigDecimal> weights = normalizedCurrentWeights(restaurantId, List.of(
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
		Map<PricingWeightCode, BigDecimal> weights = normalizedCurrentWeights(restaurantId, List.of(
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

	private Map<PricingWeightCode, BigDecimal> normalizedCurrentWeights(UUID restaurantId, List<PricingWeightCode> codes) {
		Map<PricingWeightCode, BigDecimal> source = new EnumMap<>(PricingWeightCode.class);
		codes.forEach(code -> source.put(code, weightProvider.getWeight(restaurantId, code)));
		BigDecimal sum = source.values().stream()
				.filter(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (sum.compareTo(BigDecimal.ZERO) <= 0) {
			source.clear();
			codes.forEach(code -> source.put(code, properties.getDefaults().getWeights().getOrDefault(code.name(), BigDecimal.ZERO)));
		}
		Map<PricingWeightCode, BigDecimal> normalized = NormalizationUtils.normalizeWeights(source);
		Map<PricingWeightCode, BigDecimal> result = new EnumMap<>(PricingWeightCode.class);
		codes.forEach(code -> result.put(code, normalized.getOrDefault(code, BigDecimal.ZERO)));
		return result;
	}

	private BigDecimal value(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private double average(double[] values) {
		double total = 0;
		for (double value : values) {
			total += value;
		}
		return total / values.length;
	}

}

