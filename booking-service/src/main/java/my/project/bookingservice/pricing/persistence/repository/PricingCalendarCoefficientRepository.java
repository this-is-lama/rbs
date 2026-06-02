package my.project.bookingservice.pricing.persistence.repository;

import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.persistence.entity.PricingCalendarCoefficientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PricingCalendarCoefficientRepository extends JpaRepository<PricingCalendarCoefficientEntity, Long> {
	Optional<PricingCalendarCoefficientEntity> findByRestaurantIdAndCalendarDayType(UUID restaurantId, CalendarDayType calendarDayType);
}

