package my.project.bookingservice.pricing.blocks.calendar;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.blocks.BlockCalculationResult;
import my.project.bookingservice.pricing.blocks.PricingBlock;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingBlockCode;
import my.project.bookingservice.pricing.parameters.calendar.CalendarStatusParameter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CalendarContextBlock implements PricingBlock {
	private final CalendarStatusParameter calendarStatusParameter;

	@Override
	public BlockCalculationResult calculate(PricingContext context) {
		BigDecimal value = calendarStatusParameter.calculate(context).value();
		return new BlockCalculationResult(PricingBlockCode.CALENDAR_CONTEXT, value, Map.of("calendarStatus", value));
	}
}

