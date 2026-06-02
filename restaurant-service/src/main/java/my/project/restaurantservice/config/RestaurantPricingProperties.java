package my.project.restaurantservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "pricing")
@Getter
@Setter
public class RestaurantPricingProperties {
	private Defaults defaults = new Defaults();
	private SystemLimits systemLimits = new SystemLimits();

	@Getter
	@Setter
	public static class Defaults {
		private BigDecimal minPricingCharge = BigDecimal.valueOf(100);
		private BigDecimal maxPricingCharge = BigDecimal.valueOf(1000);
	}

	@Getter
	@Setter
	public static class SystemLimits {
		private BigDecimal minPricingCharge = BigDecimal.ZERO;
		private BigDecimal maxPricingCharge = BigDecimal.valueOf(10000);
	}
}

