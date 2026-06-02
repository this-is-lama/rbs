package my.project.bookingservice.pricing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class HistoryBlendUtils {
	private HistoryBlendUtils() {
	}

	public static BigDecimal lambda(long successfulCount, int minThreshold, int fullThreshold) {
		if (successfulCount < minThreshold) {
			return BigDecimal.ZERO;
		}
		if (fullThreshold <= minThreshold || successfulCount >= fullThreshold) {
			return BigDecimal.ONE;
		}
		BigDecimal value = BigDecimal.valueOf(successfulCount - minThreshold)
				.divide(BigDecimal.valueOf(fullThreshold - minThreshold), 10, RoundingMode.HALF_UP);
		return NormalizationUtils.clamp01(value);
	}

	public static BigDecimal blend(BigDecimal defaultValue,
								   BigDecimal historicalValue,
								   long successfulCount,
								   int minThreshold,
								   int fullThreshold) {
		BigDecimal safeDefault = defaultValue == null ? BigDecimal.ZERO : defaultValue;
		if (historicalValue == null || successfulCount < minThreshold) {
			return NormalizationUtils.clamp01(safeDefault);
		}
		if (fullThreshold <= minThreshold || successfulCount >= fullThreshold) {
			return NormalizationUtils.clamp01(historicalValue);
		}
		BigDecimal lambda = lambda(successfulCount, minThreshold, fullThreshold);
		BigDecimal normalizedLambda = NormalizationUtils.clamp01(lambda);
		BigDecimal value = BigDecimal.ONE.subtract(normalizedLambda).multiply(safeDefault)
				.add(normalizedLambda.multiply(historicalValue));
		return NormalizationUtils.clamp01(value);
	}
}
