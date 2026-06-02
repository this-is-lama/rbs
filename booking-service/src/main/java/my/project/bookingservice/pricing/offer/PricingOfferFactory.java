package my.project.bookingservice.pricing.offer;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.calculator.PricingCalculationResult;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import my.project.bookingservice.pricing.settings.PricingProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PricingOfferFactory {
	private final PricingProperties properties;

	public PricingOfferEntity create(PricingContext context, PricingCalculationResult result) {
		Instant now = Instant.now();
		PricingOfferEntity entity = new PricingOfferEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(context.userId());
		entity.setRestaurantId(context.restaurantId());
		entity.setTableId(context.tableId());
		entity.setCartHash(context.cartHash());
		entity.setVisitStart(context.visitStart());
		entity.setVisitEnd(context.visitEnd());
		entity.setPreorderAmount(result.preorderAmount());
		entity.setPricingCharge(result.pricingCharge());
		entity.setTotalAmount(result.totalAmount());
		entity.setDemandIndex(result.demandIndex());
		entity.setLoadBlockValue(result.loadBlockValue());
		entity.setHistoricalDemandBlockValue(result.historicalDemandBlockValue());
		entity.setCalendarContextBlockValue(result.calendarContextBlockValue());
		entity.setCurrency(properties.getCurrency());
		entity.setStatus(PricingOfferStatus.ACTIVE);
		entity.setCalculatedAt(result.calculatedAt());
		entity.setExpiresAt(now.plusSeconds(properties.getOfferTtlMinutes() * 60L));
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		return entity;
	}
}

