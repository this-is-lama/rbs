package my.project.bookingservice.pricing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TimeIntervalUtils {
	private TimeIntervalUtils() {
	}

	public static String resolveTimeIntervalCode(LocalTime time) {
		if (time == null) return "UNKNOWN";
		if (!time.isBefore(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(11, 0))) return "BREAKFAST";
		if (!time.isBefore(LocalTime.of(11, 0)) && time.isBefore(LocalTime.of(16, 0))) return "LUNCH";
		if (!time.isBefore(LocalTime.of(16, 0)) && time.isBefore(LocalTime.of(22, 0))) return "EVENING_PEAK";
		return "LATE_EVENING";
	}

	public static Map<String, BigDecimal> resolveIntervalShares(LocalTime start, LocalTime end) {
		Map<String, BigDecimal> shares = new LinkedHashMap<>();
		if (start == null || end == null || start.equals(end)) {
			return shares;
		}

		int startMinute = start.toSecondOfDay() / 60;
		int endMinute = end.toSecondOfDay() / 60;
		if (endMinute <= startMinute) {
			endMinute += 24 * 60;
		}

		int totalMinutes = endMinute - startMinute;
		addOverlap(shares, "LATE_EVENING", startMinute, endMinute, 0, 6 * 60, totalMinutes);
		addOverlap(shares, "BREAKFAST", startMinute, endMinute, 6 * 60, 11 * 60, totalMinutes);
		addOverlap(shares, "LUNCH", startMinute, endMinute, 11 * 60, 16 * 60, totalMinutes);
		addOverlap(shares, "EVENING_PEAK", startMinute, endMinute, 16 * 60, 22 * 60, totalMinutes);
		addOverlap(shares, "LATE_EVENING", startMinute, endMinute, 22 * 60, 24 * 60, totalMinutes);
		addOverlap(shares, "LATE_EVENING", startMinute, endMinute, 24 * 60, 30 * 60, totalMinutes);
		addOverlap(shares, "BREAKFAST", startMinute, endMinute, 30 * 60, 35 * 60, totalMinutes);
		addOverlap(shares, "LUNCH", startMinute, endMinute, 35 * 60, 40 * 60, totalMinutes);
		addOverlap(shares, "EVENING_PEAK", startMinute, endMinute, 40 * 60, 46 * 60, totalMinutes);
		addOverlap(shares, "LATE_EVENING", startMinute, endMinute, 46 * 60, 48 * 60, totalMinutes);
		return shares;
	}

	private static void addOverlap(Map<String, BigDecimal> shares,
								   String code,
								   int startMinute,
								   int endMinute,
								   int intervalStart,
								   int intervalEnd,
								   int totalMinutes) {
		int overlap = Math.max(0, Math.min(endMinute, intervalEnd) - Math.max(startMinute, intervalStart));
		if (overlap == 0) {
			return;
		}
		BigDecimal share = BigDecimal.valueOf(overlap)
				.divide(BigDecimal.valueOf(totalMinutes), 10, RoundingMode.HALF_UP);
		shares.merge(code, share, BigDecimal::add);
	}
}

