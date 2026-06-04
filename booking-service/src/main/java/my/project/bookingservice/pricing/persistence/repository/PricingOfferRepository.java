package my.project.bookingservice.pricing.persistence.repository;

import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Active temporary pricing offers are not loaded from PostgreSQL at runtime; Redis cache is the source for live offers.
public interface PricingOfferRepository extends JpaRepository<PricingOfferEntity, UUID> {
}

