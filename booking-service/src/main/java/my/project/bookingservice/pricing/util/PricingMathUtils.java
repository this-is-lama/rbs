package my.project.bookingservice.pricing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PricingMathUtils {
	private PricingMathUtils() {
	}

	public static BigDecimal sigmoidPricingCharge(
			BigDecimal demandIndex,
			BigDecimal minCharge,
			BigDecimal maxCharge,
			BigDecimal k,
			BigDecimal center
	) {
		double d = NormalizationUtils.clamp01(demandIndex).doubleValue();
		double min = minCharge.doubleValue();
		double max = maxCharge.doubleValue();
		double steepness = k.doubleValue();
		double middle = center.doubleValue();
		double sigmoid = 1.0 / (1.0 + Math.exp(-steepness * (d - middle)));
		double value = min + (max - min) * sigmoid;
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal money(BigDecimal value) {
		if (value == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal clampMoney(BigDecimal value, BigDecimal min, BigDecimal max) {
		BigDecimal money = money(value);
		if (money.compareTo(min) < 0) {
			return money(min);
		}
		if (money.compareTo(max) > 0) {
			return money(max);
		}
		return money;
	}
}

