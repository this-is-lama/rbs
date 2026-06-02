package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.history.PricingHistoryReadinessService;
import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingHistoryReadinessServiceTest {
	@Test
	void historyIsReadyOnlyWhenPastSuccessfulBookingsReachThreshold() {
		UUID restaurantId = UUID.randomUUID();
		PricingHistoryService historyService = mock(PricingHistoryService.class);
		PricingProperties properties = new PricingProperties();
		properties.getHistory().setMinBookingsForWeightHistory(3);
		PricingHistoryReadinessService service = new PricingHistoryReadinessService(historyService, properties);

		when(historyService.countSuccessfulBookings(restaurantId)).thenReturn(2L);
		assertThat(service.hasEnoughHistoryForWeights(restaurantId)).isFalse();

		when(historyService.countSuccessfulBookings(restaurantId)).thenReturn(3L);
		assertThat(service.hasEnoughHistoryForWeights(restaurantId)).isTrue();
	}
}
