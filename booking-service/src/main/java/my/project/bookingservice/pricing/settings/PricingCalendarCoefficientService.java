package my.project.bookingservice.pricing.settings;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.persistence.repository.PricingCalendarCoefficientRepository;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingCalendarCoefficientService {
	private final PricingCalendarCoefficientRepository calendarCoefficientRepository;
	private final PricingProperties properties;

	public BigDecimal getCalendarCoefficient(UUID restaurantId, CalendarDayType dayType) {
		return getCalendarCoefficientValue(restaurantId, dayType).value();
	}

	@Cacheable(cacheNames = "calendarCoefficients", key = "#restaurantId + ':' + #dayType")
	public CalendarCoefficientValue getCalendarCoefficientValue(UUID restaurantId, CalendarDayType dayType) {
		return calendarCoefficientRepository.findByRestaurantIdAndCalendarDayType(restaurantId, dayType)
				.map(entity -> new CalendarCoefficientValue(
						normalizedOrDefault(entity.getCoefficientValue(), dayType),
						entity.getSource() == null ? PricingValueSource.HISTORICAL : entity.getSource(),
						entity.getObservationsCount() == null ? 0 : entity.getObservationsCount()
				))
				.orElseGet(() -> new CalendarCoefficientValue(
						normalizedOrDefault(null, dayType),
						PricingValueSource.DEFAULT,
						0
				));
	}

	public BigDecimal normalizeParameter(BigDecimal value) {
		return NormalizationUtils.clamp01(value);
	}

	public record CalendarCoefficientValue(BigDecimal value, PricingValueSource source, int observationsCount) {
	}

	private BigDecimal normalizedOrDefault(BigDecimal value, CalendarDayType dayType) {
		BigDecimal resolved = value == null
				? properties.getDefaults().getCalendarCoefficients().getOrDefault(dayType.name(), BigDecimal.ZERO)
				: value;
		return NormalizationUtils.clamp01(resolved);
	}
}

