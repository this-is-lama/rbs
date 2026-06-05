package my.project.bookingservice.pricing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.project.bookingservice.pricing.enums.PricingValueSource;
import my.project.bookingservice.pricing.enums.PricingWeightCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "pricing_weight",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_pricing_weight_restaurant_code",
				columnNames = {
						"restaurant_id",
						"weight_code"
				}
		),
		indexes = {
				@Index(
						name = "idx_pricing_weight_restaurant_id",
						columnList = "restaurant_id"
				)
		}
)
public class PricingWeightEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
	private UUID restaurantId;

	@Enumerated(EnumType.STRING)
	@Column(name = "weight_code", nullable = false)
	private PricingWeightCode weightCode;

	@Column(precision = 10, scale = 6)
	private BigDecimal weightValue;

	@Enumerated(EnumType.STRING)
	private PricingValueSource source;

	private Instant updatedAt;
}

