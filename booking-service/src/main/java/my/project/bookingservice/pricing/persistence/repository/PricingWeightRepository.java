package my.project.bookingservice.pricing.persistence.repository;

import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.persistence.entity.PricingWeightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingWeightRepository extends JpaRepository<PricingWeightEntity, Long> {
	Optional<PricingWeightEntity> findByRestaurantIdAndWeightCode(UUID restaurantId, PricingWeightCode weightCode);
	List<PricingWeightEntity> findAllByRestaurantId(UUID restaurantId);
}

