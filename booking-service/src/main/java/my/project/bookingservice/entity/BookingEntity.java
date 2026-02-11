package my.project.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "bookings",
		indexes = {
				@Index(name = "idx_bookings_table_id", columnList = "table_id"),
				@Index(name = "idx_bookings_restaurant_id", columnList = "restaurant_id"),
				@Index(name = "idx_bookings_user_id", columnList = "user_id"),
				@Index(name = "idx_bookings_status", columnList = "status"),
				@Index(name = "idx_bookings_start_at", columnList = "start_at"),
				@Index(name = "idx_bookings_end_at", columnList = "end_at")
		}
)
public class BookingEntity {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
	private UUID restaurantId;

	@Column(name = "user_id", nullable = false, columnDefinition = "uuid")
	private UUID userId;

	@Column(name = "table_id", nullable = false, columnDefinition = "uuid")
	private UUID tableId;

	@Column(name = "start_at", nullable = false)
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	private Instant endAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private BookingStatus status;

	@Column(name = "guests", nullable = false)
	private Integer guests;

	@Column(name = "comment", length = 500)
	private String comment;

	@Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
	private BigDecimal totalAmount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

	@OneToMany(
			mappedBy = "booking",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<DishEntity> dishes = new ArrayList<>();

	@OneToOne(mappedBy = "booking",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private TableEntity table;

	@OneToOne(mappedBy = "booking",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private RestaurantEntity restaurant;

	public void setRestaurant(RestaurantEntity restaurant) {
		this.restaurant = restaurant;
		if (restaurant != null) {
			restaurant.setBooking(this);
			this.restaurantId = restaurant.getRestaurantId();
		}
	}

	public void setTable(TableEntity table) {
		this.table = table;
		if (table != null) {
			table.setBooking(this);
			this.tableId = table.getTableId();
		}
	}

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) createdAt = now;
		if (status == null) status = BookingStatus.RESERVED;
		syncTableId();
		this.totalAmount = dishes.stream()
				.map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@PreUpdate
	void syncTableId() {
		if (this.tableId == null && this.table != null) {
			this.tableId = this.table.getTableId();
		}
	}

	public void addDish(DishEntity dish) {
		dishes.add(dish);
		dish.setBooking(this);
	}

	public void removeDish(DishEntity dish) {
		dishes.remove(dish);
		dish.setBooking(null);
	}

	public boolean isCancelled() {
		return status == BookingStatus.CANCELLED;
	}

	public void cancel(Instant now) {
		if (!isCancelled()) {
			this.status = BookingStatus.CANCELLED;
			this.cancelledAt = now;
		}
	}
}
