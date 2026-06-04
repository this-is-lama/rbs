package my.project.bookingservice.pricing.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.project.bookingservice.pricing.enums.CalendarDataSource;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "pricing_calendar_day",
		uniqueConstraints = @UniqueConstraint(columnNames = "calendar_date")
)
public class PricingCalendarDayEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "calendar_date", nullable = false, unique = true)
	private LocalDate calendarDate;

	@Column(name = "day_off", nullable = false)
	private boolean dayOff;

	@Column(name = "holiday", nullable = false)
	private boolean holiday;

	@Enumerated(EnumType.STRING)
	@Column(name = "source", nullable = false, length = 20)
	private CalendarDataSource source;

	@Column(name = "loaded_at", nullable = false)
	private Instant loadedAt;
}
