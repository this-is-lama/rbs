package my.project.bookingservice.pricing;

import my.project.bookingservice.client.RestaurantServiceClient;
import my.project.bookingservice.dto.client.RestaurantBookingPricingSummaryResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.parameters.ParameterCalculationResult;
import my.project.bookingservice.pricing.parameters.calendar.CalendarStatusParameter;
import my.project.bookingservice.pricing.persistence.entity.PricingHistorySnapshotEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingHistorySnapshotRepository;
import my.project.bookingservice.pricing.scheduler.PricingHistorySnapshotScheduler;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static my.project.bookingservice.pricing.enums.PricingParameterCode.CALENDAR_STATUS;
import static my.project.bookingservice.pricing.enums.PricingValueSource.DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingHistorySnapshotSchedulerTest {
    @Test
    void createsTableLevelSnapshotForPreviousDay() {
        UUID restaurantId = UUID.randomUUID();
        UUID tableA = UUID.randomUUID();
        UUID tableB = UUID.randomUUID();
        LocalDate date = LocalDate.now(BookingHelper.BUSINESS_ZONE).minusDays(1);
        Instant from = date.atStartOfDay(BookingHelper.BUSINESS_ZONE).toInstant();
        BookingEntity first = booking(restaurantId, tableA, from.plusSeconds(12 * 3600), from.plusSeconds(11 * 3600));
        BookingEntity second = booking(restaurantId, tableA, from.plusSeconds(13 * 3600), from.plusSeconds(11 * 3600));
        BookingEntity third = booking(restaurantId, tableB, from.plusSeconds(12 * 3600), from.plusSeconds(10 * 3600));

        BookingRepository bookingRepository = mock(BookingRepository.class);
        when(bookingRepository.findRestaurantIdsWithBookingsBetween(any(), any(), any())).thenReturn(List.of(restaurantId));
        when(bookingRepository.findAllByRestaurantIdAndStatusAndStartAtGreaterThanEqualAndStartAtLessThan(any(), any(), any(), any()))
                .thenReturn(List.of(first, second, third));

        RestaurantServiceClient restaurantClient = mock(RestaurantServiceClient.class);
        when(restaurantClient.bookingPricingSummary(restaurantId))
                .thenReturn(new RestaurantBookingPricingSummaryResponse(restaurantId, 4, null, null));

        PricingHistorySnapshotRepository snapshotRepository = mock(PricingHistorySnapshotRepository.class);
        when(snapshotRepository.existsByRestaurantIdAndObservationDateAndTimeIntervalCodeAndObservationType(any(), any(), any(), any()))
                .thenReturn(false);
        when(snapshotRepository.existsByRestaurantIdAndObservationDateAndTimeIntervalCodeAndTableIdAndObservationType(any(), any(), any(), any(), any()))
                .thenReturn(false);

        PricingHistoryService historyService = mock(PricingHistoryService.class);
        when(historyService.maxSuccessfulBookingsByWeekday(restaurantId)).thenReturn(10L);
        when(historyService.countSuccessfulBookingsByWeekday(any(), any())).thenReturn(5L);
        when(historyService.maxSuccessfulBookingsByTimeInterval(restaurantId)).thenReturn(6L);
        when(historyService.countSuccessfulBookingsByTimeInterval(restaurantId, "LUNCH")).thenReturn(3L);
        when(historyService.maxSuccessfulBookingsByTable(restaurantId)).thenReturn(10L);
        when(historyService.countSuccessfulBookingsByTable(restaurantId, tableA)).thenReturn(7L);
        when(historyService.countSuccessfulBookingsByTable(restaurantId, tableB)).thenReturn(3L);

        CalendarStatusParameter calendar = mock(CalendarStatusParameter.class);
        when(calendar.calculate(any())).thenReturn(new ParameterCalculationResult(CALENDAR_STATUS, new BigDecimal("0.5"), DEFAULT));

        PricingHistorySnapshotScheduler scheduler = new PricingHistorySnapshotScheduler(
                bookingRepository,
                restaurantClient,
                snapshotRepository,
                new PricingProperties(),
                calendar,
                historyService
        );

        scheduler.buildSnapshots();

        ArgumentCaptor<PricingHistorySnapshotEntity> captor = ArgumentCaptor.forClass(PricingHistorySnapshotEntity.class);
        verify(snapshotRepository, org.mockito.Mockito.times(3)).save(captor.capture());
        PricingHistorySnapshotEntity restaurantSnapshot = captor.getAllValues().stream()
                .filter(snapshot -> snapshot.getObservationType() == PricingHistoryObservationType.RESTAURANT_INTERVAL)
                .findFirst()
                .orElseThrow();
        PricingHistorySnapshotEntity tableASnapshot = captor.getAllValues().stream()
                .filter(snapshot -> tableA.equals(snapshot.getTableId()))
                .findFirst()
                .orElseThrow();

        assertThat(restaurantSnapshot.getTableId()).isNull();
        assertThat(restaurantSnapshot.getObservationDate()).isEqualTo(date);
        assertThat(restaurantSnapshot.getSuccessfulBookingsCount()).isEqualTo(3);
        assertThat(restaurantSnapshot.getAvailableTablesCount()).isEqualTo(4);
        assertThat(restaurantSnapshot.getRealizedDemandValue()).isEqualByComparingTo("0.7500000000");
        assertThat(restaurantSnapshot.getOccupancyValue()).isEqualByComparingTo("0.5000000000");
        assertThat(restaurantSnapshot.getTableDemandValue()).isNull();

        assertThat(tableASnapshot.getObservationDate()).isEqualTo(date);
        assertThat(tableASnapshot.getObservationType()).isEqualTo(PricingHistoryObservationType.TABLE_INTERVAL);
        assertThat(tableASnapshot.getSuccessfulBookingsCount()).isEqualTo(2);
        assertThat(tableASnapshot.getAvailableTablesCount()).isEqualTo(1);
        assertThat(tableASnapshot.getRealizedDemandValue()).isEqualByComparingTo("1");
        assertThat(tableASnapshot.getOccupancyValue()).isEqualByComparingTo("0.5000000000");
        assertThat(tableASnapshot.getTableDemandValue()).isEqualByComparingTo("0.7000000000");
    }

    @Test
    void skipsSnapshotWhenTotalTablesIsZero() {
        UUID restaurantId = UUID.randomUUID();
        LocalDate date = LocalDate.now(BookingHelper.BUSINESS_ZONE).minusDays(1);
        Instant from = date.atStartOfDay(BookingHelper.BUSINESS_ZONE).toInstant();

        BookingRepository bookingRepository = mock(BookingRepository.class);
        when(bookingRepository.findRestaurantIdsWithBookingsBetween(any(), any(), any())).thenReturn(List.of(restaurantId));
        RestaurantServiceClient restaurantClient = mock(RestaurantServiceClient.class);
        when(restaurantClient.bookingPricingSummary(restaurantId))
                .thenReturn(new RestaurantBookingPricingSummaryResponse(restaurantId, 0, null, null));
        PricingHistorySnapshotRepository snapshotRepository = mock(PricingHistorySnapshotRepository.class);

        PricingHistorySnapshotScheduler scheduler = new PricingHistorySnapshotScheduler(
                bookingRepository,
                restaurantClient,
                snapshotRepository,
                new PricingProperties(),
                mock(CalendarStatusParameter.class),
                mock(PricingHistoryService.class)
        );

        scheduler.buildSnapshots();

        verify(snapshotRepository, never()).save(any(PricingHistorySnapshotEntity.class));
    }

    private BookingEntity booking(UUID restaurantId, UUID tableId, Instant startAt, Instant createdAt) {
        BookingEntity entity = new BookingEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setRestaurantId(restaurantId);
        entity.setTableId(tableId);
        entity.setStartAt(startAt);
        entity.setEndAt(startAt.plusSeconds(3600));
        entity.setCreatedAt(createdAt);
        entity.setStatus(BookingStatus.RESERVED);
        return entity;
    }
}
