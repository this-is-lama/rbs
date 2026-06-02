package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import my.project.bookingservice.pricing.parameters.historical.TimeIntervalDemandParameter;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static my.project.bookingservice.pricing.PricingTestSupport.context;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeIntervalDemandParameterTest {
    @Test
    void calculatesSingleIntervalDemand() {
        PricingHistoryService history = mock(PricingHistoryService.class);
        UUID restaurantId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        when(history.countSuccessfulBookings(restaurantId)).thenReturn(150L);
        when(history.maxSuccessfulBookingsByTimeInterval(restaurantId)).thenReturn(10L);
        when(history.countSuccessfulBookingsByTimeInterval(restaurantId, "LUNCH")).thenReturn(5L);

        BigDecimal value = new TimeIntervalDemandParameter(history, new PricingProperties(), new HistoricalTransitionService())
                .calculate(context(restaurantId, tableId, Instant.parse("2026-06-01T09:00:00Z"))).value();

        assertThat(value).isEqualByComparingTo("0.50000000000");
    }

    @Test
    void calculatesWeightedDemandAcrossTwoIntervals() {
        PricingHistoryService history = mock(PricingHistoryService.class);
        UUID restaurantId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        when(history.countSuccessfulBookings(restaurantId)).thenReturn(150L);
        when(history.maxSuccessfulBookingsByTimeInterval(restaurantId)).thenReturn(10L);
        when(history.countSuccessfulBookingsByTimeInterval(restaurantId, "LUNCH")).thenReturn(4L);
        when(history.countSuccessfulBookingsByTimeInterval(restaurantId, "EVENING_PEAK")).thenReturn(8L);

        BigDecimal value = new TimeIntervalDemandParameter(history, new PricingProperties(), new HistoricalTransitionService())
                .calculate(context(
                        restaurantId,
                        tableId,
                        Instant.parse("2026-06-01T12:00:00Z"),
                        1,
                        10,
                        BigDecimal.TEN,
                        Instant.parse("2026-05-31T12:00:00Z"),
                        Instant.parse("2026-06-01T14:00:00Z")
                )).value();

        assertThat(value).isEqualByComparingTo("0.60000000000");
    }

    @Test
    void returnsDefaultWhenHistoryIsInsufficient() {
        PricingHistoryService history = mock(PricingHistoryService.class);

        BigDecimal value = new TimeIntervalDemandParameter(history, new PricingProperties(), new HistoricalTransitionService())
                .calculate(context(1, 10)).value();

        assertThat(value).isEqualByComparingTo("0.5");
    }
}
