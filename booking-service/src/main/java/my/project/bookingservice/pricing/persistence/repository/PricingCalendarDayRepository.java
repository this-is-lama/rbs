package my.project.bookingservice.pricing.persistence.repository;

import my.project.bookingservice.pricing.persistence.entity.PricingCalendarDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PricingCalendarDayRepository extends JpaRepository<PricingCalendarDayEntity, Long> {
	Optional<PricingCalendarDayEntity> findByCalendarDate(LocalDate calendarDate);
}
