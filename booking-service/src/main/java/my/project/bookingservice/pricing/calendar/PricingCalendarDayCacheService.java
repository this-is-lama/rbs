package my.project.bookingservice.pricing.calendar;

import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class PricingCalendarDayCacheService {
	@CachePut(cacheNames = "pricingCalendarDays", key = "#value.date()")
	public PricingCalendarDayValue put(PricingCalendarDayValue value) {
		return value;
	}
}
