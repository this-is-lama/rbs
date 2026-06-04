package my.project.bookingservice.pricing.offer;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.calculator.PricingCalculationResult;
import my.project.bookingservice.pricing.cache.PricingOfferCacheDto;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PricingOfferFactory {
	private final PricingProperties properties;

	public PricingOfferCacheDto createCacheDto(PricingContext context, PricingCalculationResult result) {
		Instant now = Instant.now();
		return new PricingOfferCacheDto(
				UUID.randomUUID(),
				context.userId(),
				context.restaurantId(),
				context.tableId(),
				context.visitStart(),
				context.visitEnd(),
				context.cartHash(),
				result.preorderAmount(),
				result.pricingCharge(),
				result.totalAmount(),
				result.demandIndex(),
				result.loadBlockValue(),
				result.historicalDemandBlockValue(),
				result.calendarContextBlockValue(),
				properties.getCurrency(),
				PricingOfferStatus.ACTIVE,
				result.calculatedAt(),
				now.plusSeconds(properties.getOfferTtlMinutes() * 60L)
		);
	}

}

