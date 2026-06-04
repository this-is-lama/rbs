package my.project.bookingservice.pricing.calendar;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.parameters.calendar.CalendarClassifier;
import my.project.bookingservice.pricing.persistence.entity.PricingCalendarCoefficientEntity;
import my.project.bookingservice.pricing.persistence.entity.PricingHistorySnapshotEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingCalendarCoefficientRepository;
import my.project.bookingservice.pricing.persistence.repository.PricingHistorySnapshotRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingCalendarCoefficientUpdateService {
	private final PricingHistorySnapshotRepository snapshotRepository;
	private final PricingCalendarCoefficientRepository coefficientRepository;
	private final CalendarClassifier calendarClassifier;
	private final PricingProperties properties;
	private final PricingCalendarCoefficientCacheEvictService coefficientCacheEvictService;

	@Transactional
	public int update(UUID restaurantId) {
		LocalDate to = LocalDate.now(BookingTimeUtils.BUSINESS_ZONE);
		LocalDate from = to.minusDays(properties.getHistory().getPeriodDays());
		List<PricingHistorySnapshotEntity> snapshots = snapshotRepository.findAllByRestaurantIdAndObservationTypeAndObservationDateBetween(
				restaurantId,
				PricingHistoryObservationType.RESTAURANT_INTERVAL,
				from,
				to
		);
		if (snapshots.isEmpty()) {
			return 0;
		}

		Map<CalendarDayType, Accumulator> totals = new EnumMap<>(CalendarDayType.class);
		for (CalendarDayType dayType : CalendarDayType.values()) {
			totals.put(dayType, new Accumulator());
		}

		for (PricingHistorySnapshotEntity snapshot : snapshots) {
			if (snapshot.getObservationDate() == null || snapshot.getSuccessfulBookingsCount() == null) {
				continue;
			}
			var membership = calendarClassifier.classify(snapshot.getObservationDate());
			membership.degrees().forEach((dayType, degree) -> {
				BigDecimal normalizedDegree = degree == null ? BigDecimal.ZERO : NormalizationUtils.clamp01(degree);
				if (normalizedDegree.compareTo(BigDecimal.ZERO) > 0) {
					totals.get(dayType).add(snapshot.getSuccessfulBookingsCount(), normalizedDegree);
				}
			});
		}

		Map<CalendarDayType, BigDecimal> intensities = new EnumMap<>(CalendarDayType.class);
		BigDecimal maxIntensity = BigDecimal.ZERO;
		for (Map.Entry<CalendarDayType, Accumulator> entry : totals.entrySet()) {
			if (entry.getValue().observations.compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			BigDecimal intensity = entry.getValue().bookings.divide(entry.getValue().observations, 10, RoundingMode.HALF_UP);
			intensities.put(entry.getKey(), intensity);
			if (intensity.compareTo(maxIntensity) > 0) {
				maxIntensity = intensity;
			}
		}
		if (maxIntensity.compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}

		int updated = 0;
		for (Map.Entry<CalendarDayType, BigDecimal> entry : intensities.entrySet()) {
			Accumulator accumulator = totals.get(entry.getKey());
			if (accumulator.observations.compareTo(BigDecimal.valueOf(properties.getHistory().getMinObservationsForCalendarClass())) < 0) {
				continue;
			}
			BigDecimal coefficient = NormalizationUtils.clamp01(entry.getValue().divide(maxIntensity, 10, RoundingMode.HALF_UP));
			PricingCalendarCoefficientEntity entity = coefficientRepository.findByRestaurantIdAndCalendarDayType(restaurantId, entry.getKey())
					.orElseGet(PricingCalendarCoefficientEntity::new);
			entity.setRestaurantId(restaurantId);
			entity.setCalendarDayType(entry.getKey());
			entity.setCoefficientValue(coefficient);
			entity.setObservationsCount(accumulator.observations.setScale(0, RoundingMode.HALF_UP).intValue());
			entity.setSource(PricingValueSource.HISTORICAL);
			entity.setUpdatedAt(Instant.now());
			coefficientRepository.save(entity);
			coefficientCacheEvictService.evict(restaurantId, entry.getKey());
			updated++;
		}
		return updated;
	}

	private static class Accumulator {
		private BigDecimal bookings = BigDecimal.ZERO;
		private BigDecimal observations = BigDecimal.ZERO;

		private void add(int bookingsCount, BigDecimal degree) {
			bookings = bookings.add(BigDecimal.valueOf(bookingsCount).multiply(degree));
			observations = observations.add(degree);
		}
	}
}


