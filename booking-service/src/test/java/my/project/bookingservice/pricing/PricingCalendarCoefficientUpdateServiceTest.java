package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.calendar.PricingCalendarCoefficientUpdateService;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.parameters.calendar.CalendarMembership;
import my.project.bookingservice.pricing.persistence.entity.PricingCalendarCoefficientEntity;
import my.project.bookingservice.pricing.persistence.entity.PricingHistorySnapshotEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingCalendarCoefficientRepository;
import my.project.bookingservice.pricing.persistence.repository.PricingHistorySnapshotRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingCalendarCoefficientUpdateServiceTest {
    @Test
    void updatesCoefficientsFromRestaurantIntervalIntensity() {
        UUID restaurantId = UUID.randomUUID();
        PricingHistorySnapshotRepository snapshotRepository = mock(PricingHistorySnapshotRepository.class);
        when(snapshotRepository.findAllByRestaurantIdAndObservationTypeAndObservationDateBetween(any(), any(), any(), any()))
                .thenReturn(List.of(
                        snapshot(restaurantId, "2026-06-01", 10),
                        snapshot(restaurantId, "2026-06-02", 10),
                        snapshot(restaurantId, "2026-06-06", 5),
                        snapshot(restaurantId, "2026-06-07", 5)
                ));

        PricingCalendarCoefficientRepository coefficientRepository = mock(PricingCalendarCoefficientRepository.class);
        when(coefficientRepository.findByRestaurantIdAndCalendarDayType(any(), any())).thenReturn(Optional.empty());
		when(coefficientRepository.save(any(PricingCalendarCoefficientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PricingProperties properties = new PricingProperties();
		properties.getHistory().setMinObservationsForCalendarClass(2);
        PricingCalendarCoefficientUpdateService service = new PricingCalendarCoefficientUpdateService(
                snapshotRepository,
                coefficientRepository,
                date -> membership(date.getDayOfWeek().getValue() >= 6 ? CalendarDayType.WEEKEND : CalendarDayType.WORKDAY),
                properties
        );

        int updated = service.update(restaurantId);

        ArgumentCaptor<PricingCalendarCoefficientEntity> captor = ArgumentCaptor.forClass(PricingCalendarCoefficientEntity.class);
        verify(coefficientRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Map<CalendarDayType, PricingCalendarCoefficientEntity> saved = captor.getAllValues().stream()
                .collect(Collectors.toMap(PricingCalendarCoefficientEntity::getCalendarDayType, entity -> entity));

        assertThat(updated).isEqualTo(2);
        assertThat(saved.get(CalendarDayType.WORKDAY).getCoefficientValue()).isEqualByComparingTo("1.0000000000");
        assertThat(saved.get(CalendarDayType.WEEKEND).getCoefficientValue()).isEqualByComparingTo("0.5000000000");
        assertThat(saved.get(CalendarDayType.WORKDAY).getObservationsCount()).isEqualTo(2);
        assertThat(saved.get(CalendarDayType.WORKDAY).getSource()).isEqualTo(PricingValueSource.HISTORICAL);
    }

    private PricingHistorySnapshotEntity snapshot(UUID restaurantId, String date, int successfulBookings) {
        PricingHistorySnapshotEntity entity = new PricingHistorySnapshotEntity();
        entity.setRestaurantId(restaurantId);
        entity.setObservationType(PricingHistoryObservationType.RESTAURANT_INTERVAL);
        entity.setObservationDate(LocalDate.parse(date));
        entity.setSuccessfulBookingsCount(successfulBookings);
        return entity;
    }

    private CalendarMembership membership(CalendarDayType activeType) {
        Map<CalendarDayType, BigDecimal> degrees = new EnumMap<>(CalendarDayType.class);
        for (CalendarDayType dayType : CalendarDayType.values()) {
            degrees.put(dayType, dayType == activeType ? BigDecimal.ONE : BigDecimal.ZERO);
        }
        return new CalendarMembership(degrees);
    }
}
