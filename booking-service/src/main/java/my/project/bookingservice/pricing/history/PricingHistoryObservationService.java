package my.project.bookingservice.pricing.history;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.history.model.PricingHistoryObservation;
import my.project.bookingservice.pricing.persistence.entity.PricingHistorySnapshotEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingHistorySnapshotRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.service.BookingHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingHistoryObservationService {
	private final PricingHistorySnapshotRepository repository;
	private final PricingProperties properties;

	public List<PricingHistoryObservation> getObservationsForWeightUpdate(UUID restaurantId) {
		return getRestaurantIntervalObservationsForWeightUpdate(restaurantId);
	}

	public List<PricingHistoryObservation> getRestaurantIntervalObservationsForWeightUpdate(UUID restaurantId) {
		return getObservations(
				restaurantId,
				PricingHistoryObservationType.RESTAURANT_INTERVAL,
				properties.getHistory().getMinBookingsForWeightHistory()
		);
	}

	public List<PricingHistoryObservation> getTableIntervalObservationsForWeightUpdate(UUID restaurantId) {
		return getObservations(
				restaurantId,
				PricingHistoryObservationType.TABLE_INTERVAL,
				properties.getHistory().getMinBookingsForWeightHistory()
		);
	}

	private List<PricingHistoryObservation> getObservations(UUID restaurantId,
															PricingHistoryObservationType observationType,
															int minObservations) {
		LocalDate to = LocalDate.now(BookingHelper.BUSINESS_ZONE);
		LocalDate from = to.minusDays(properties.getHistory().getPeriodDays());
		List<PricingHistorySnapshotEntity> snapshots = repository.findAllByRestaurantIdAndObservationTypeAndObservationDateBetween(
				restaurantId,
				observationType,
				from,
				to
		);
		if (snapshots.size() < minObservations) {
			return List.of();
		}
		return snapshots.stream()
				.map(snapshot -> new PricingHistoryObservation(
						snapshot.getRestaurantId(),
						snapshot.getTableId(),
						snapshot.getObservationType(),
						snapshot.getObservationDate(),
						snapshot.getTimeIntervalCode(),
						snapshot.getSuccessfulBookingsCount(),
						snapshot.getAvailableTablesCount(),
						snapshot.getOccupancyValue(),
						snapshot.getUrgencyValue(),
						snapshot.getWeekdayDemandValue(),
						snapshot.getTimeIntervalDemandValue(),
						snapshot.getTableDemandValue(),
						snapshot.getCalendarStatusValue(),
						snapshot.getRealizedDemandValue()
				))
				.toList();
	}
}

