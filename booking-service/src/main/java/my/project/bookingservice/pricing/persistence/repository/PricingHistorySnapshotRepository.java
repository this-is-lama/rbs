package my.project.bookingservice.pricing.persistence.repository;

import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.persistence.entity.PricingHistorySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PricingHistorySnapshotRepository extends JpaRepository<PricingHistorySnapshotEntity, Long> {
	List<PricingHistorySnapshotEntity> findAllByRestaurantIdAndObservationDateBetween(UUID restaurantId, LocalDate from, LocalDate to);

	List<PricingHistorySnapshotEntity> findAllByRestaurantIdAndObservationTypeAndObservationDateBetween(
			UUID restaurantId,
			PricingHistoryObservationType observationType,
			LocalDate from,
			LocalDate to
	);

	boolean existsByRestaurantIdAndObservationDateAndTimeIntervalCodeAndObservationType(
			UUID restaurantId,
			LocalDate observationDate,
			String timeIntervalCode,
			PricingHistoryObservationType observationType
	);

	boolean existsByRestaurantIdAndObservationDateAndTimeIntervalCodeAndTableIdAndObservationType(
			UUID restaurantId,
			LocalDate observationDate,
			String timeIntervalCode,
			UUID tableId,
			PricingHistoryObservationType observationType
	);
}

