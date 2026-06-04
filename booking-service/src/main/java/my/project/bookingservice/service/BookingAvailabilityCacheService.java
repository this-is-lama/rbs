package my.project.bookingservice.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BookingAvailabilityCacheService {
	@CacheEvict(
			cacheNames = "bookingAvailability",
			key = "#restaurantId + ':' + #tableId + ':' + #date"
	)
	public void evict(UUID restaurantId, UUID tableId, LocalDate date) {
	}
}
