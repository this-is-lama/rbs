package my.project.bookingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "booking_restaurant",
		indexes = @Index(name = "idx_booking_restaurant_restaurant_id", columnList = "restaurant_id")
)
public class RestaurantEntity {

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

	@Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
	private UUID restaurantId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "category", nullable = false, length = 100)
	private String category;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "address", nullable = false)
	private String address;
}
