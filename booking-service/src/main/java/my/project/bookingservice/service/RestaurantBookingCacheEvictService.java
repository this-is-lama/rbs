package my.project.bookingservice.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RestaurantBookingCacheEvictService {

	@CacheEvict(cacheNames = "restaurantBookings", key = "#restaurantId")
	public void evict(UUID restaurantId) {
	}
}
