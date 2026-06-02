package my.project.bookingservice.pricing.parameters.load;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingParameterCode;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.exception.PricingCalculationException;
import my.project.bookingservice.pricing.parameters.ParameterCalculationResult;
import my.project.bookingservice.pricing.parameters.PricingParameter;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class UrgencyParameter implements PricingParameter {
	private final PricingProperties properties;

	@Override
	public ParameterCalculationResult calculate(PricingContext context) {
		long minutes = Duration.between(context.requestTime(), context.visitStart()).toMinutes();
		if (minutes <= 0) {
			throw new PricingCalculationException("Visit start must be in the future");
		}
		BigDecimal scale = properties.getHistory().getUrgencyScaleHours();
		if (scale == null || scale.compareTo(BigDecimal.ZERO) <= 0) {
			throw new PricingCalculationException("Urgency scale hours must be greater than zero");
		}
		BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 10, RoundingMode.HALF_UP);
		double value = Math.exp(-hours.doubleValue() / scale.doubleValue());
		return new ParameterCalculationResult(PricingParameterCode.URGENCY, NormalizationUtils.clamp01(BigDecimal.valueOf(value)), PricingValueSource.DEFAULT);
	}
}

