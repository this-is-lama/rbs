package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricalTransitionServiceTest {
	private final HistoricalTransitionService service = new HistoricalTransitionService();

	@Test
	void returnsDefaultBeforeMinimumThreshold() {
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("0.9"), 49, 50, 150))
				.isEqualByComparingTo("0.5");
	}

	@Test
	void returnsDefaultAtMinimumThreshold() {
		assertThat(service.lambda(50, 50, 150)).isEqualByComparingTo("0");
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("0.9"), 50, 50, 150))
				.isEqualByComparingTo("0.50000000000");
	}

	@Test
	void blendsBetweenMinimumAndFullThreshold() {
		assertThat(service.lambda(100, 50, 150)).isEqualByComparingTo("0.5000000000");
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("0.9"), 100, 50, 150))
				.isEqualByComparingTo("0.70000000000");
	}

	@Test
	void returnsHistoricalAtAndAfterFullThreshold() {
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("0.9"), 150, 50, 150))
				.isEqualByComparingTo("0.9");
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("0.9"), 200, 50, 150))
				.isEqualByComparingTo("0.9");
	}

	@Test
	void returnsDefaultWhenHistoricalValueIsMissing() {
		assertThat(service.blend(new BigDecimal("0.5"), null, 150, 50, 150))
				.isEqualByComparingTo("0.5");
	}

	@Test
	void clampsResultToUnitInterval() {
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("2.0"), 150, 50, 150))
				.isEqualByComparingTo("1");
	}

	@Test
	void fullThresholdAtMinimumUsesHistoricalImmediatelyAtMinimum() {
		assertThat(service.blend(new BigDecimal("0.5"), new BigDecimal("0.9"), 50, 50, 50))
				.isEqualByComparingTo("0.9");
	}
}
