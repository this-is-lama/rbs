package my.project.bookingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name="booking_table",
		indexes = @Index(name="idx_booking_table_table_id", columnList="table_id")
)

public class TableEntity {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(columnDefinition = "uuid")
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "booking_id",
			nullable = false,
			unique = true
	)
	private BookingEntity booking;

	@Column(name = "table_id", nullable = false, columnDefinition = "uuid")
	private UUID tableId;

	@Column(name = "table_number", nullable = false)
	private int tableNumber;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	@Column(name = "description")
	private String description;
}
