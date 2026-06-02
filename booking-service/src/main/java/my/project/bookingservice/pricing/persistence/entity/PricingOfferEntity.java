package my.project.bookingservice.pricing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pricing_offer")
public class PricingOfferEntity {
	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "user_id", nullable = false, columnDefinition = "uuid")
	private UUID userId;

	@Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
	private UUID restaurantId;

	@Column(name = "table_id", nullable = false, columnDefinition = "uuid")
	private UUID tableId;

	@Column(nullable = false, length = 128)
	private String cartHash;

	@Column(name = "visit_start", nullable = false)
	private Instant visitStart;

	@Column(name = "visit_end", nullable = false)
	private Instant visitEnd;

	@Column(precision = 19, scale = 2)
	private BigDecimal preorderAmount;
	@Column(precision = 19, scale = 2)
	private BigDecimal pricingCharge;
	@Column(precision = 19, scale = 2)
	private BigDecimal totalAmount;
	@Column(precision = 10, scale = 6)
	private BigDecimal demandIndex;
	@Column(precision = 10, scale = 6)
	private BigDecimal loadBlockValue;
	@Column(precision = 10, scale = 6)
	private BigDecimal historicalDemandBlockValue;
	@Column(precision = 10, scale = 6)
	private BigDecimal calendarContextBlockValue;
	private String currency;
	@Enumerated(EnumType.STRING)
	private PricingOfferStatus status;
	private Instant calculatedAt;
	private Instant expiresAt;
	private Instant createdAt;
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private Long version;
}

