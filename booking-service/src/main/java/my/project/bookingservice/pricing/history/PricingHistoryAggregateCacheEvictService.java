package my.project.bookingservice.pricing.history;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PricingHistoryAggregateCacheEvictService {
	@CacheEvict(cacheNames = "pricingHistoryAggregates", key = "#restaurantId")
	public void evict(UUID restaurantId) {
	}
}
