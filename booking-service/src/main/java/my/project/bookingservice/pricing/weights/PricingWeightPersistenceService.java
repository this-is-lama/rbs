package my.project.bookingservice.pricing.weights;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.persistence.entity.PricingWeightEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingWeightRepository;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.weights.result.WeightCalculationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingWeightPersistenceService {
	private final PricingWeightProvider weightProvider;
	private final PricingWeightUpdater weightUpdater;
	private final PricingWeightRepository weightRepository;
	private final PricingWeightCacheEvictService weightCacheEvictService;

	public List<WeightCalculationResult.Item> apply(
			UUID restaurantId,
			Map<PricingWeightCode, BigDecimal> recommendedWeights
	) {
		if (recommendedWeights == null || recommendedWeights.isEmpty()) {
			return List.of();
		}

		Map<PricingWeightCode, BigDecimal> updatedWeights = new EnumMap<>(PricingWeightCode.class);
		recommendedWeights.forEach((code, recommended) -> {
			BigDecimal current = weightProvider.getWeight(restaurantId, code);
			BigDecimal updated = weightUpdater.smoothUpdate(current, recommended);
			updatedWeights.put(code, updated);
		});

		Map<PricingWeightCode, BigDecimal> normalizedUpdatedWeights = NormalizationUtils.normalizeWeights(updatedWeights);
		List<WeightCalculationResult.Item> items = new ArrayList<>();
		recommendedWeights.forEach((code, recommended) -> {
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
		weightCacheEvictService.evict(restaurantId, code);
	}
}
