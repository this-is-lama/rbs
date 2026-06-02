package my.project.bookingservice.pricing;

import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.pricing.scheduler.PricingWeightsUpdateScheduler;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.weights.PricingWeightCalculationService;
import my.project.bookingservice.pricing.weights.result.WeightCalculationResult;
import my.project.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingWeightsUpdateSchedulerTest {
	@Test
	void selectsRestaurantsUsingPastHistoryWindow() {
		UUID restaurantId = UUID.randomUUID();
		BookingRepository bookingRepository = mock(BookingRepository.class);
		when(bookingRepository.findRestaurantIdsWithAtLeastSuccessfulBookingsBetween(any(), any(), any(), anyLong()))
				.thenReturn(List.of(restaurantId));
		PricingWeightCalculationService calculationService = mock(PricingWeightCalculationService.class);
		when(calculationService.calculateRecommendedWeights(restaurantId))
				.thenReturn(new WeightCalculationResult(restaurantId, List.of()));
		PricingProperties properties = new PricingProperties();
		properties.getHistory().setMinBookingsForWeightHistory(5);

		new PricingWeightsUpdateScheduler(calculationService, bookingRepository, properties).updateWeights();

		ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
		ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
		verify(bookingRepository).findRestaurantIdsWithAtLeastSuccessfulBookingsBetween(
				org.mockito.ArgumentMatchers.eq(BookingStatus.RESERVED),
				fromCaptor.capture(),
				toCaptor.capture(),
				org.mockito.ArgumentMatchers.eq(5L)
		);
		assertThat(fromCaptor.getValue()).isBefore(toCaptor.getValue());
		assertThat(toCaptor.getValue()).isBeforeOrEqualTo(Instant.now());
		verify(calculationService).calculateRecommendedWeights(restaurantId);
		verify(bookingRepository, never()).findRestaurantIdsWithAtLeastSuccessfulBookings(any(), anyLong());
	}
}
