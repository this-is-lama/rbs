package my.project.bookingservice.pricing.context;

import my.project.bookingservice.dto.client.BookingDishDto;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.util.PricingMathUtils;
import my.project.common.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PricingPreorderAmountCalculator {
	public BigDecimal calculate(List<PricingPreorderItemRequest> items, List<BookingDishDto> dishes) {
		if (items == null || items.isEmpty()) {
			return BigDecimal.ZERO;
		}
		Map<UUID, BookingDishDto> dishById = dishes == null
				? Map.of()
				: dishes.stream().collect(Collectors.toMap(BookingDishDto::id, Function.identity()));

		BigDecimal amount = BigDecimal.ZERO;
		for (PricingPreorderItemRequest item : items) {
			BookingDishDto dish = dishById.get(item.dishId());
			if (dish == null) {
				throw new ValidationException("pricing.dish-not-found", item.dishId());
			}
			amount = amount.add(dish.price().multiply(BigDecimal.valueOf(item.quantity())));
		}
		return PricingMathUtils.money(amount);
	}
}
