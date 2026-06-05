package my.project.bookingservice.pricing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.enums.PricingValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "pricing_calendar_coefficient",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_pricing_calendar_coefficient_restaurant_type",
				columnNames = {
						"restaurant_id",
						"calendar_day_type"
				}
		),
		indexes = {
				@Index(
						name = "idx_pricing_calendar_coefficient_restaurant",
						columnList = "restaurant_id"
				)
		}
)
public class PricingCalendarCoefficientEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
	private UUID restaurantId;

	@Enumerated(EnumType.STRING)
	@Column(name = "calendar_day_type", nullable = false)
	private CalendarDayType calendarDayType;

	@Column(precision = 10, scale = 6)
	private BigDecimal coefficientValue;

	private Integer observationsCount;

	@Enumerated(EnumType.STRING)
	private PricingValueSource source;

	private Instant updatedAt;

}

