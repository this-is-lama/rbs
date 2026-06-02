package my.project.bookingservice.pricing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NormalizationUtils {
	private static final int SCALE = 10;

	private NormalizationUtils() {
	}

	public static BigDecimal clamp01(BigDecimal value) {
		if (value == null) return BigDecimal.ZERO;
		if (value.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
		if (value.compareTo(BigDecimal.ONE) > 0) return BigDecimal.ONE;
		return value;
	}

	public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
		if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
	}

	public static <K> Map<K, BigDecimal> normalizeWeights(Map<K, BigDecimal> source) {
		Map<K, BigDecimal> result = new LinkedHashMap<>();
		BigDecimal sum = source.values().stream()
				.filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (sum.compareTo(BigDecimal.ZERO) == 0) {
			source.forEach((key, value) -> result.put(key, BigDecimal.ZERO));
			return result;
		}
		source.forEach((key, value) -> result.put(key, value == null ? BigDecimal.ZERO : value.divide(sum, SCALE, RoundingMode.HALF_UP)));
		return result;
	}
}

