package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.persistence.entity.PricingWeightEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingWeightRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.weights.PricingWeightService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingWeightProviderTest {
    @Test
    void manualWeightIsAlwaysUsed() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(weight(PricingValueSource.MANUAL, "0.77"), 0);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.77");
    }

    @Test
    void historicalWeightUsesDefaultBeforeWeightHistoryMinimum() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(weight(PricingValueSource.HISTORICAL, "0.8"), 49);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.4");
    }

    @Test
    void historicalWeightUsesDefaultAtWeightHistoryMinimum() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(weight(PricingValueSource.HISTORICAL, "0.8"), 50);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.40000000000");
    }

    @Test
    void historicalWeightIsBlendedBetweenWeightHistoryMinimumAndFullThreshold() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(weight(PricingValueSource.HISTORICAL, "0.8"), 100);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.60000000000");
    }

    @Test
    void historicalWeightIsUsedAtAndAfterFullWeightHistoryThreshold() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService atFull = serviceWith(weight(PricingValueSource.HISTORICAL, "0.8"), 150);
        PricingWeightService afterFull = serviceWith(weight(PricingValueSource.HISTORICAL, "0.8"), 200);

        assertThat(atFull.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.8");
        assertThat(afterFull.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.8");
    }

    @Test
    void nullOrMissingWeightFallsBackToDefault() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(weight(PricingValueSource.HISTORICAL, null), 200);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.4");
    }

    @Test
    void negativeWeightFallsBackToDefault() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(weight(PricingValueSource.MANUAL, "-0.1"), 200);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.4");
    }

    @Test
    void missingRestaurantWeightFallsBackToDefault() {
        UUID restaurantId = UUID.randomUUID();
        PricingWeightService service = serviceWith(null, 200);

        assertThat(service.getWeight(restaurantId, PricingWeightCode.LOAD_BLOCK)).isEqualByComparingTo("0.4");
    }

    private PricingWeightService serviceWith(PricingWeightEntity entity, long successfulCount) {
        PricingWeightRepository repository = mock(PricingWeightRepository.class);
        when(repository.findByRestaurantIdAndWeightCode(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.ofNullable(entity));
        PricingHistoryService historyService = mock(PricingHistoryService.class);
        when(historyService.countSuccessfulBookings(org.mockito.ArgumentMatchers.any())).thenReturn(successfulCount);
        return new PricingWeightService(repository, new PricingProperties(), historyService, new HistoricalTransitionService());
    }

    private PricingWeightEntity weight(PricingValueSource source, String value) {
        PricingWeightEntity entity = new PricingWeightEntity();
        entity.setSource(source);
        entity.setWeightValue(value == null ? null : new BigDecimal(value));
        return entity;
    }
}
