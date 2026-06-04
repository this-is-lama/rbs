package my.project.bookingservice.pricing.weights;

import my.project.bookingservice.pricing.history.model.PricingHistoryObservation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Service
public class PricingCorrelationService {
	public BigDecimal positiveCorrelation(List<PricingHistoryObservation> observations,
										  Function<PricingHistoryObservation, BigDecimal> valueExtractor) {
		List<double[]> pairs = observations.stream()
				.map(observation -> new BigDecimal[]{
						valueExtractor.apply(observation),
						observation.realizedDemandValue()
				})
				.filter(pair -> pair[0] != null && pair[1] != null)
				.map(pair -> new double[]{pair[0].doubleValue(), pair[1].doubleValue()})
				.toList();
		double[] x = pairs.stream().mapToDouble(pair -> pair[0]).toArray();
		double[] y = pairs.stream().mapToDouble(pair -> pair[1]).toArray();
		if (x.length < 2) {
			return BigDecimal.ZERO;
		}
		double xAvg = average(x);
		double yAvg = average(y);
		double numerator = 0;
		double xVariance = 0;
		double yVariance = 0;
		for (int i = 0; i < x.length; i++) {
			double dx = x[i] - xAvg;
			double dy = y[i] - yAvg;
			numerator += dx * dy;
			xVariance += dx * dx;
			yVariance += dy * dy;
		}
		if (xVariance == 0 || yVariance == 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(Math.max(0, numerator / Math.sqrt(xVariance * yVariance)));
	}

	private double average(double[] values) {
		double total = 0;
		for (double value : values) {
			total += value;
		}
		return total / values.length;
	}
}
