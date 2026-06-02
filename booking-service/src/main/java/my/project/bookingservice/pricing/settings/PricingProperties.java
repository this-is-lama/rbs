package my.project.bookingservice.pricing.settings;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {
	private int offerTtlMinutes = 10;
	private String currency = "RUB";
	private Sigmoid sigmoid = new Sigmoid();
	private History history = new History();
	private Calendar calendar = new Calendar();
	private SystemLimits systemLimits = new SystemLimits();
	private Defaults defaults = new Defaults();

	@PostConstruct
	public void validate() {
		if (history.fullBookingsForWeightHistory < history.minBookingsForWeightHistory) {
			throw new IllegalStateException(
					"fullBookingsForWeightHistory must be greater than or equal to minBookingsForWeightHistory"
			);
		}
	}

	@Getter
	@Setter
	public static class Sigmoid {
		private BigDecimal k = BigDecimal.valueOf(8);
		private BigDecimal center = BigDecimal.valueOf(0.5);
	}

	@Getter
	@Setter
	public static class History {
		private int periodDays = 90;
		private int minBookingsForHistory = 50;
		private int fullBookingsForHistory = 150;
		private int minBookingsForTableHistory = 50;
		private int fullBookingsForTableHistory = 150;
		private int minObservationsForCalendarClass = 5;
		private int fullObservationsForCalendarClass = 20;
		private int minBookingsForWeightHistory = 50;
		private int fullBookingsForWeightHistory = 150;
		private BigDecimal weightUpdateRate = BigDecimal.valueOf(0.1);
		private BigDecimal urgencyScaleHours = BigDecimal.valueOf(24);
	}

	@Getter
	@Setter
	public static class SystemLimits {
		private BigDecimal minPricingCharge = BigDecimal.ZERO;
		private BigDecimal maxPricingCharge = BigDecimal.valueOf(10000);
	}

	@Getter
	@Setter
	public static class Calendar {
		private String countryCode = "ru";
		private List<LocalDate> holidays = new ArrayList<>();
		private List<LocalDate> peakHolidays = new ArrayList<>();
	}

	@Getter
	@Setter
	public static class Defaults {
		private BigDecimal restaurantMinPricingCharge = BigDecimal.valueOf(100);
		private BigDecimal restaurantMaxPricingCharge = BigDecimal.valueOf(1000);
		private BigDecimal weekdayDemand = BigDecimal.valueOf(0.5);
		private BigDecimal timeIntervalDemand = BigDecimal.valueOf(0.5);
		private BigDecimal tableDemand = BigDecimal.valueOf(0.5);
		private BigDecimal neutralCalendarContext = BigDecimal.valueOf(0.5);
		private Map<String, BigDecimal> weights = new HashMap<>();
		private Map<String, BigDecimal> calendarCoefficients = new HashMap<>();

		public Defaults() {
			weights.put("LOAD_BLOCK", BigDecimal.valueOf(0.4));
			weights.put("HISTORICAL_DEMAND_BLOCK", BigDecimal.valueOf(0.4));
			weights.put("CALENDAR_CONTEXT_BLOCK", BigDecimal.valueOf(0.2));
			weights.put("OCCUPANCY_PARAMETER", BigDecimal.valueOf(0.4));
			weights.put("URGENCY_PARAMETER", BigDecimal.valueOf(0.4));
			weights.put("OCCUPANCY_URGENCY_INTERACTION", BigDecimal.valueOf(0.2));
			weights.put("WEEKDAY_DEMAND_PARAMETER", BigDecimal.valueOf(0.35));
			weights.put("TIME_INTERVAL_DEMAND_PARAMETER", BigDecimal.valueOf(0.45));
			weights.put("TABLE_DEMAND_PARAMETER", BigDecimal.valueOf(0.2));

			calendarCoefficients.put("WORKDAY", BigDecimal.valueOf(0.35));
			calendarCoefficients.put("WEEKEND", BigDecimal.valueOf(0.65));
			calendarCoefficients.put("HOLIDAY", BigDecimal.valueOf(0.8));
			calendarCoefficients.put("PEAK_HOLIDAY", BigDecimal.valueOf(1.0));
		}
	}
}

