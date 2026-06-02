package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.settings.PricingProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingPropertiesTest {
	@Test
	void rejectsWeightFullThresholdBelowMinimumThreshold() {
		PricingProperties properties = new PricingProperties();
		properties.getHistory().setMinBookingsForWeightHistory(50);
		properties.getHistory().setFullBookingsForWeightHistory(49);

		assertThatThrownBy(properties::validate)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("fullBookingsForWeightHistory");
	}
}
