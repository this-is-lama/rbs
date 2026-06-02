package my.project.bookingservice.pricing.persistence.repository;

import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PricingOfferRepository extends JpaRepository<PricingOfferEntity, UUID> {
	Optional<PricingOfferEntity> findFirstByUserIdAndRestaurantIdAndTableIdAndCartHashAndVisitStartAndVisitEndAndStatusOrderByCreatedAtDesc(
			UUID userId,
			UUID restaurantId,
			UUID tableId,
			String cartHash,
			Instant visitStart,
			Instant visitEnd,
			PricingOfferStatus status
	);
}

