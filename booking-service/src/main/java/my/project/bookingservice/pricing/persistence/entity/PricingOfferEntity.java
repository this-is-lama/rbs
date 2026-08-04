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
@Table(
		name = "pricing_offer",
		indexes = {
				@Index(
						name = "idx_pricing_offer_user_cart_expires",
						columnList = "user_id, cart_hash, expires_at"
				),
				@Index(
						name = "idx_pricing_offer_restaurant_table_expires",
						columnList = "restaurant_id, table_id, expires_at"
				),
				@Index(
						name = "idx_pricing_offer_status_expires",
						columnList = "status, expires_at"
				)
		}
)
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

	@Column(name = "cart_hash", nullable = false, length = 128)
	private String cartHash;

	@Column(name = "preorder_amount", precision = 19, scale = 2)
	private BigDecimal preorderAmount;

	@Column(name = "pricing_charge", precision = 19, scale = 2)
	private BigDecimal pricingCharge;

	@Column(name = "total_amount", precision = 19, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "demand_index_value", precision = 10, scale = 6)
	private BigDecimal demandIndexValue;

	@Column(name = "load_block_value", precision = 10, scale = 6)
	private BigDecimal loadBlockValue;

	@Column(name = "historical_demand_block_value", precision = 10, scale = 6)
	private BigDecimal historicalDemandBlockValue;

	@Column(name = "calendar_context_block_value", precision = 10, scale = 6)
	private BigDecimal calendarContextBlockValue;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private PricingOfferStatus status;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private Long version;

	@PrePersist
	private void prePersist() {
		Instant now = Instant.now();

		if (id == null) {
			id = UUID.randomUUID();
		}

		if (createdAt == null) {
			createdAt = now;
		}

		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	private void preUpdate() {
		updatedAt = Instant.now();
	}
}