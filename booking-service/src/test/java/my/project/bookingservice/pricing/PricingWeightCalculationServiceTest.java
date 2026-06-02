package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.history.PricingHistoryObservationService;
import my.project.bookingservice.pricing.history.model.PricingHistoryObservation;
import my.project.bookingservice.pricing.persistence.entity.PricingWeightEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingWeightRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.weights.PricingWeightCalculationService;
import my.project.bookingservice.pricing.weights.PricingWeightProvider;
import my.project.bookingservice.pricing.weights.PricingWeightUpdater;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingWeightCalculationServiceTest {
    @Test
    void weightsAreNormalizedInsideEachModelLevel() {
        UUID restaurantId = UUID.randomUUID();
        PricingHistoryObservationService observations = mock(PricingHistoryObservationService.class);
        when(observations.getRestaurantIntervalObservationsForWeightUpdate(restaurantId)).thenReturn(List.of(
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.1"),
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.5"),
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.9")
        ));
        when(observations.getTableIntervalObservationsForWeightUpdate(restaurantId)).thenReturn(List.of(
                observation(restaurantId, PricingHistoryObservationType.TABLE_INTERVAL, "0.1"),
                observation(restaurantId, PricingHistoryObservationType.TABLE_INTERVAL, "0.5"),
                observation(restaurantId, PricingHistoryObservationType.TABLE_INTERVAL, "0.9")
        ));
        PricingProperties properties = new PricingProperties();
        properties.getHistory().setWeightUpdateRate(BigDecimal.ONE);
        PricingWeightProvider provider = (id, code) -> BigDecimal.ONE;
        PricingWeightRepository repository = mock(PricingWeightRepository.class);
        when(repository.findByRestaurantIdAndWeightCode(any(), any())).thenReturn(Optional.empty());
        when(repository.save(any(PricingWeightEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PricingWeightCalculationService service = new PricingWeightCalculationService(
                observations,
                provider,
                new PricingWeightUpdater(properties),
                repository,
                properties
        );

        service.calculateRecommendedWeights(restaurantId);

        ArgumentCaptor<PricingWeightEntity> captor = ArgumentCaptor.forClass(PricingWeightEntity.class);
        verify(repository, org.mockito.Mockito.times(9)).save(captor.capture());
        Map<PricingWeightCode, BigDecimal> saved = captor.getAllValues().stream()
                .collect(Collectors.toMap(PricingWeightEntity::getWeightCode, PricingWeightEntity::getWeightValue));

        assertGroupSumsToOne(saved, PricingWeightCode.LOAD_BLOCK, PricingWeightCode.HISTORICAL_DEMAND_BLOCK, PricingWeightCode.CALENDAR_CONTEXT_BLOCK);
        assertGroupSumsToOne(saved, PricingWeightCode.OCCUPANCY_PARAMETER, PricingWeightCode.URGENCY_PARAMETER, PricingWeightCode.OCCUPANCY_URGENCY_INTERACTION);
        assertGroupSumsToOne(saved, PricingWeightCode.WEEKDAY_DEMAND_PARAMETER, PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER, PricingWeightCode.TABLE_DEMAND_PARAMETER);
    }

    @Test
    void zeroImportanceDoesNotUpdateWeights() {
        UUID restaurantId = UUID.randomUUID();
        PricingHistoryObservationService observations = mock(PricingHistoryObservationService.class);
        when(observations.getRestaurantIntervalObservationsForWeightUpdate(restaurantId)).thenReturn(List.of(
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.5"),
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.5")
        ));
        when(observations.getTableIntervalObservationsForWeightUpdate(restaurantId)).thenReturn(List.of(
                observation(restaurantId, PricingHistoryObservationType.TABLE_INTERVAL, "0.5"),
                observation(restaurantId, PricingHistoryObservationType.TABLE_INTERVAL, "0.5")
        ));
        PricingProperties properties = new PricingProperties();
        PricingWeightRepository repository = mock(PricingWeightRepository.class);

        PricingWeightCalculationService service = new PricingWeightCalculationService(
                observations,
                (id, code) -> BigDecimal.ONE,
                new PricingWeightUpdater(properties),
                repository,
                properties
        );

        service.calculateRecommendedWeights(restaurantId);

        verify(repository, never()).save(any(PricingWeightEntity.class));
    }

    @Test
    void historicalWeekdayAndTimeWeightsAreUpdatedWithoutTableObservations() {
        UUID restaurantId = UUID.randomUUID();
        PricingHistoryObservationService observations = mock(PricingHistoryObservationService.class);
        when(observations.getRestaurantIntervalObservationsForWeightUpdate(restaurantId)).thenReturn(List.of(
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.1"),
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.5"),
                observation(restaurantId, PricingHistoryObservationType.RESTAURANT_INTERVAL, "0.9")
        ));
        when(observations.getTableIntervalObservationsForWeightUpdate(restaurantId)).thenReturn(List.of());
        PricingProperties properties = new PricingProperties();
        properties.getHistory().setWeightUpdateRate(BigDecimal.ONE);
        PricingWeightRepository repository = mock(PricingWeightRepository.class);
        when(repository.findByRestaurantIdAndWeightCode(any(), any())).thenReturn(Optional.empty());
        when(repository.save(any(PricingWeightEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PricingWeightCalculationService service = new PricingWeightCalculationService(
                observations,
                (id, code) -> BigDecimal.ONE,
                new PricingWeightUpdater(properties),
                repository,
                properties
        );

        service.calculateRecommendedWeights(restaurantId);

        ArgumentCaptor<PricingWeightEntity> captor = ArgumentCaptor.forClass(PricingWeightEntity.class);
        verify(repository, org.mockito.Mockito.times(9)).save(captor.capture());
        Map<PricingWeightCode, BigDecimal> saved = captor.getAllValues().stream()
                .collect(Collectors.toMap(PricingWeightEntity::getWeightCode, PricingWeightEntity::getWeightValue));

        assertThat(saved.get(PricingWeightCode.WEEKDAY_DEMAND_PARAMETER)).isGreaterThan(BigDecimal.ZERO);
        assertThat(saved.get(PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER)).isGreaterThan(BigDecimal.ZERO);
        assertThat(saved.get(PricingWeightCode.TABLE_DEMAND_PARAMETER)).isEqualByComparingTo(BigDecimal.ZERO);
        assertGroupSumsToOne(saved, PricingWeightCode.WEEKDAY_DEMAND_PARAMETER, PricingWeightCode.TIME_INTERVAL_DEMAND_PARAMETER, PricingWeightCode.TABLE_DEMAND_PARAMETER);
    }

    private PricingHistoryObservation observation(UUID restaurantId, PricingHistoryObservationType observationType, String value) {
        BigDecimal v = new BigDecimal(value);
        return new PricingHistoryObservation(
                restaurantId,
                UUID.randomUUID(),
                observationType,
                LocalDate.parse("2026-06-01"),
                "LUNCH",
                1,
                10,
                v,
                v,
                v,
                v,
                v,
                v,
                v
        );
    }

    private void assertGroupSumsToOne(Map<PricingWeightCode, BigDecimal> values, PricingWeightCode... codes) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PricingWeightCode code : codes) {
            sum = sum.add(values.get(code));
        }
        assertThat(sum).isCloseTo(BigDecimal.ONE, org.assertj.core.api.Assertions.within(new BigDecimal("0.000000001")));
    }
}
