package my.project.bookingservice.pricing.calendar;

import my.project.bookingservice.pricing.enums.CalendarDayType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PricingCalendarCoefficientCacheEvictService {
	@CacheEvict(cacheNames = "calendarCoefficients", key = "#restaurantId + ':' + #dayType")
	public void evict(UUID restaurantId, CalendarDayType dayType) {
	}
}
