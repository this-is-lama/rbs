package my.project.bookingservice.pricing.parameters;

import my.project.bookingservice.pricing.context.PricingContext;

public interface PricingParameter {
	ParameterCalculationResult calculate(PricingContext context);
}

