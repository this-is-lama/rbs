package my.project.bookingservice.pricing.weights;

import my.project.bookingservice.pricing.enums.PricingWeightCode;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PricingWeightCacheEvictService {
	@CacheEvict(cacheNames = "pricingWeights", key = "#restaurantId + ':' + #code")
	public void evict(UUID restaurantId, PricingWeightCode code) {
	}
}
