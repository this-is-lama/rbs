package my.project.bookingservice.pricing.blocks;

import my.project.bookingservice.pricing.context.PricingContext;

public interface PricingBlock {
	BlockCalculationResult calculate(PricingContext context);
}

