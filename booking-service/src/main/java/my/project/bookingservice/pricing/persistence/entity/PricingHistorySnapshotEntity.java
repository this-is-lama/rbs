package my.project.bookingservice.pricing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.project.bookingservice.pricing.enums.PricingHistoryObservationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "pricing_history_snapshot",
		indexes = {
				@Index(
						name = "idx_pricing_history_restaurant_date",
						columnList = "restaurant_id, observation_date"
				),
				@Index(
						name = "idx_pricing_history_restaurant_type_date",
						columnList = "restaurant_id, observation_type, observation_date"
				),
				@Index(
						name = "idx_pricing_history_restaurant_date_interval_type",
						columnList = "restaurant_id, observation_date, time_interval_code, observation_type"
				),
				@Index(
						name = "idx_pricing_history_restaurant_date_interval_table_type",
						columnList = "restaurant_id, observation_date, time_interval_code, table_id, observation_type"
				)
		}
)
public class PricingHistorySnapshotEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
	private UUID restaurantId;

	@Column(name = "table_id", columnDefinition = "uuid")
	private UUID tableId;

	@Enumerated(EnumType.STRING)
	@Column(name = "observation_type", nullable = false, length = 40)
	private PricingHistoryObservationType observationType;

	private LocalDate observationDate;

	private String timeIntervalCode;

	private Integer successfulBookingsCount;

	private Integer availableTablesCount;

	@Column(precision = 10, scale = 6)
	private BigDecimal occupancyValue;

	@Column(precision = 10, scale = 6)
	private BigDecimal urgencyValue;

	@Column(precision = 10, scale = 6)
	private BigDecimal weekdayDemandValue;

	@Column(precision = 10, scale = 6)
	private BigDecimal timeIntervalDemandValue;

	@Column(precision = 10, scale = 6)
	private BigDecimal tableDemandValue;

	@Column(precision = 10, scale = 6)
	private BigDecimal calendarStatusValue;

	@Column(precision = 10, scale = 6)
	private BigDecimal realizedDemandValue;

	private Instant createdAt;
}

