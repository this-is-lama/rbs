package my.project.bookingservice.pricing.parameters.load;

import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingParameterCode;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.exception.PricingCalculationException;
import my.project.bookingservice.pricing.parameters.ParameterCalculationResult;
import my.project.bookingservice.pricing.parameters.PricingParameter;
import my.project.bookingservice.pricing.util.NormalizationUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OccupancyParameter implements PricingParameter {
	@Override
	public ParameterCalculationResult calculate(PricingContext context) {
		if (context.totalTablesCount() == null || context.totalTablesCount() <= 0) {
			throw new PricingCalculationException("Total tables count must be greater than zero");
		}
		BigDecimal occupied = BigDecimal.valueOf(Math.max(0, context.occupiedTablesCount() == null ? 0 : context.occupiedTablesCount()));
		BigDecimal total = BigDecimal.valueOf(context.totalTablesCount());
		BigDecimal value = NormalizationUtils.clamp01(NormalizationUtils.divide(occupied, total));
		return new ParameterCalculationResult(PricingParameterCode.OCCUPANCY, value, PricingValueSource.HISTORICAL);
	}
}

