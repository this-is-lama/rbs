package my.project.bookingservice.pricing.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.parameters.calendar.CalendarStatusParameter;
import my.project.bookingservice.pricing.persistence.entity.PricingHistorySnapshotEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingHistorySnapshotRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import my.project.bookingservice.pricing.util.PricingConstants;
import my.project.bookingservice.pricing.util.TimeIntervalUtils;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingHistorySnapshotBuildService {
	private final BookingRepository bookingRepository;
	private final RestaurantServiceClient restaurantClient;
	private final PricingHistorySnapshotRepository snapshotRepository;
	private final PricingProperties properties;
	private final CalendarStatusParameter calendarStatusParameter;
	private final PricingHistoryAggregateService historyAggregateService;

	@Transactional
	public void buildYesterdaySnapshots() {
		LocalDate today = LocalDate.now(BookingTimeUtils.BUSINESS_ZONE);
		LocalDate observationDate = today.minusDays(1);
		if (!observationDate.isBefore(today)) {
			log.warn("Формирование исторических снимков пропущено: дата наблюдения не находится в прошлом, observationDate={}, today={}",
					observationDate, today);
			return;
		}
		Instant from = observationDate.atStartOfDay(BookingTimeUtils.BUSINESS_ZONE).toInstant();
		Instant to = observationDate.plusDays(1).atStartOfDay(BookingTimeUtils.BUSINESS_ZONE).toInstant();
		List<UUID> restaurantIds = bookingRepository.findRestaurantIdsWithBookingsBetween(BookingStatus.RESERVED, from, to);

		log.info("Формирование исторических снимков начато: date={}, restaurants={}", observationDate, restaurantIds.size());
		restaurantIds.forEach(restaurantId -> buildRestaurantSnapshots(restaurantId, observationDate, from, to));
	}

	private void buildRestaurantSnapshots(UUID restaurantId, LocalDate observationDate, Instant from, Instant to) {
		Integer totalTablesValue = restaurantClient.bookingPricingSummary(restaurantId).totalTablesCount();
		int totalTables = totalTablesValue == null ? 0 : totalTablesValue;
		if (totalTables <= 0) {
			log.warn("Формирование исторических снимков пропущено: отсутствуют доступные столы, restaurantId={}", restaurantId);
			return;
		}

		PricingHistoryAggregate aggregate = historyAggregateService.getAggregate(restaurantId);
		List<BookingEntity> bookings = bookingRepository.findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqualAndStartAtLessThan(
				restaurantId,
				BookingStatus.RESERVED,
				from,
				to
		);
		// TODO: Build zero-demand observations for main restaurant intervals without bookings for stricter model fitting.
		Map<String, List<BookingEntity>> byInterval = bookings.stream()
				.collect(Collectors.groupingBy(booking -> TimeIntervalUtils.resolveTimeIntervalCode(
						booking.getStartAt().atZone(BookingTimeUtils.BUSINESS_ZONE).toLocalTime()
				)));
		byInterval.forEach((interval, intervalBookings) -> {
			int occupiedTablesInInterval = (int) intervalBookings.stream()
					.map(BookingEntity::getTableId)
					.distinct()
					.count();
			if (!snapshotRepository.existsByRestaurantIdAndObservationDateAndTimeIntervalCodeAndObservationType(
					restaurantId,
					observationDate,
					interval,
					PricingHistoryObservationType.RESTAURANT_INTERVAL
			)) {
				snapshotRepository.save(createRestaurantIntervalSnapshot(
						restaurantId,
						observationDate,
						interval,
						intervalBookings,
						occupiedTablesInInterval,
						totalTables,
						aggregate
				));
			}
			intervalBookings.stream()
					.collect(Collectors.groupingBy(BookingEntity::getTableId))
					.forEach((tableId, tableBookings) -> {
						if (snapshotRepository.existsByRestaurantIdAndObservationDateAndTimeIntervalCodeAndTableIdAndObservationType(
								restaurantId,
								observationDate,
								interval,
								tableId,
								PricingHistoryObservationType.TABLE_INTERVAL
						)) {
							return;
						}
						snapshotRepository.save(createTableIntervalSnapshot(
								restaurantId,
								tableId,
								observationDate,
								interval,
								tableBookings,
								occupiedTablesInInterval,
								totalTables,
								aggregate
						));
					});
		});
	}

	private PricingHistorySnapshotEntity createRestaurantIntervalSnapshot(UUID restaurantId,
																		 LocalDate observationDate,
																		 String interval,
																		 List<BookingEntity> bookings,
																		 int occupiedTablesInInterval,
																		 int totalTables,
																		 PricingHistoryAggregate aggregate) {
		int successfulBookings = bookings.size();
		BigDecimal realizedDemand = NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(successfulBookings),
				BigDecimal.valueOf(totalTables)
		));
		BigDecimal occupancyValue = NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(occupiedTablesInInterval),
				BigDecimal.valueOf(totalTables)
		));
		PricingHistorySnapshotEntity entity = new PricingHistorySnapshotEntity();
		entity.setRestaurantId(restaurantId);
		entity.setTableId(null);
		entity.setObservationType(PricingHistoryObservationType.RESTAURANT_INTERVAL);
		entity.setObservationDate(observationDate);
		entity.setTimeIntervalCode(interval);
		entity.setSuccessfulBookingsCount(successfulBookings);
		entity.setAvailableTablesCount(totalTables);
		entity.setOccupancyValue(occupancyValue);
		entity.setUrgencyValue(averageUrgency(bookings));
		entity.setWeekdayDemandValue(weekdayDemand(bookings.get(0), aggregate));
		entity.setTimeIntervalDemandValue(timeIntervalDemand(interval, aggregate));
		entity.setTableDemandValue(null);
		entity.setCalendarStatusValue(calendarStatus(restaurantId, bookings.get(0)));
		entity.setRealizedDemandValue(realizedDemand);
		entity.setCreatedAt(Instant.now());
		return entity;
	}

	private PricingHistorySnapshotEntity createTableIntervalSnapshot(UUID restaurantId,
																	 UUID tableId,
																	 LocalDate observationDate,
																	 String interval,
																	 List<BookingEntity> bookings,
																	 int occupiedTablesInInterval,
																	 int totalTables,
																	 PricingHistoryAggregate aggregate) {
		int successfulBookings = bookings.size();
		int availableTablesCount = 1;
		BigDecimal realizedDemand = NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(successfulBookings),
				BigDecimal.valueOf(availableTablesCount)
		));
		BigDecimal occupancyValue = NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(occupiedTablesInInterval),
				BigDecimal.valueOf(totalTables)
		));
		BigDecimal tableDemand = tableDemand(tableId, aggregate);

		PricingHistorySnapshotEntity entity = new PricingHistorySnapshotEntity();
		entity.setRestaurantId(restaurantId);
		entity.setTableId(tableId);
		entity.setObservationType(PricingHistoryObservationType.TABLE_INTERVAL);
		entity.setObservationDate(observationDate);
		entity.setTimeIntervalCode(interval);
		entity.setSuccessfulBookingsCount(successfulBookings);
		entity.setAvailableTablesCount(1);
		entity.setOccupancyValue(occupancyValue);
		entity.setUrgencyValue(averageUrgency(bookings));
		entity.setWeekdayDemandValue(weekdayDemand(bookings.get(0), aggregate));
		entity.setTimeIntervalDemandValue(timeIntervalDemand(interval, aggregate));
		entity.setTableDemandValue(tableDemand);
		entity.setCalendarStatusValue(calendarStatus(restaurantId, bookings.get(0)));
		entity.setRealizedDemandValue(realizedDemand);
		entity.setCreatedAt(Instant.now());
		return entity;
	}

	private BigDecimal weekdayDemand(BookingEntity booking, PricingHistoryAggregate aggregate) {
		var day = booking.getStartAt().atZone(BookingTimeUtils.BUSINESS_ZONE).getDayOfWeek();
		long max = aggregate.maxByWeekday();
		if (max <= 0) {
			return properties.getDefaults().getWeekdayDemand();
		}
		return NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(aggregate.countByWeekday(day)),
				BigDecimal.valueOf(max)
		));
	}

	private BigDecimal timeIntervalDemand(String interval, PricingHistoryAggregate aggregate) {
		long max = aggregate.maxByTimeInterval();
		if (max <= 0) {
			return properties.getDefaults().getTimeIntervalDemand();
		}
		return NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(aggregate.countByTimeInterval(interval)),
				BigDecimal.valueOf(max)
		));
	}

	private BigDecimal tableDemand(UUID tableId, PricingHistoryAggregate aggregate) {
		long max = aggregate.maxByTable();
		if (max <= 0) {
			return properties.getDefaults().getTableDemand();
		}
		return NormalizationUtils.clamp01(NormalizationUtils.divide(
				BigDecimal.valueOf(aggregate.countByTable(tableId)),
				BigDecimal.valueOf(max)
		));
	}

	private BigDecimal averageUrgency(List<BookingEntity> bookings) {
		OptionalDouble average = bookings.stream()
				.filter(booking -> booking.getCreatedAt() != null && booking.getCreatedAt().isBefore(booking.getStartAt()))
				.mapToDouble(booking -> {
					double leadTimeHours = java.time.Duration.between(booking.getCreatedAt(), booking.getStartAt()).toMinutes() / 60.0;
					return Math.exp(-leadTimeHours / properties.getHistory().getUrgencyScaleHours().doubleValue());
				})
				.average();
		if (average.isEmpty()) {
			return null;
		}
		return NormalizationUtils.clamp01(BigDecimal.valueOf(average.getAsDouble()).setScale(10, RoundingMode.HALF_UP));
	}

	private BigDecimal calendarStatus(UUID restaurantId, BookingEntity booking) {
		PricingContext context = new PricingContext(
				booking.getUserId(),
				restaurantId,
				booking.getTableId(),
				booking.getStartAt(),
				booking.getEndAt(),
				booking.getCreatedAt() == null ? booking.getStartAt() : booking.getCreatedAt(),
				List.of(),
				BigDecimal.ZERO,
				PricingConstants.HISTORY_SNAPSHOT_CART_HASH,
				properties.getDefaults().getRestaurantMinPricingCharge(),
				properties.getDefaults().getRestaurantMaxPricingCharge(),
				1,
				0
		);
		return calendarStatusParameter.calculate(context).value();
	}
}
