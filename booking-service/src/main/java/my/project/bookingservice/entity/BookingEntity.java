package my.project.bookingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
				@Index(
						name = "idx_bookings_user_created_at",
						columnList = "user_id, created_at"
				),
				@Index(
						name = "idx_bookings_restaurant_created_at",
						columnList = "restaurant_id, created_at"
				),
				@Index(
						name = "idx_bookings_restaurant_status_start_at",
						columnList = "restaurant_id, status, start_at"
				),
				@Index(
						name = "idx_bookings_restaurant_table_status_time",
						columnList = "restaurant_id, table_id, status, start_at, end_at"
				),
				@Index(
						name = "idx_bookings_pricing_offer_id",
						columnList = "pricing_offer_id"
				)
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

	@Column(name = "preorder_amount", precision = 12, scale = 2, nullable = false)
	private BigDecimal preorderAmount;

	@Column(name = "pricing_charge", precision = 12, scale = 2, nullable = false)
	private BigDecimal pricingCharge;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

	@Column(name = "cancellation_reason", length = 500)
	private String cancellationReason;

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

	@Version
	@Column(name = "version", nullable = false)
	private long version;

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

	public void addDish(DishEntity dish) {
		dishes.add(dish);
		dish.setBooking(this);
	}

	public void setPricing(BigDecimal preorderAmount, double coefficient) {
		pricingCharge = preorderAmount.multiply(BigDecimal.valueOf(coefficient));
		totalAmount = preorderAmount.add(pricingCharge);
	}

	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (status == null) {
			status = BookingStatus.RESERVED;
		}
		syncTableId();


	}

	@PreUpdate
	void syncTableId() {
		if (this.tableId == null && this.table != null) {
			this.tableId = this.table.getTableId();
		}
	}



	public boolean isCancelled() {
		return status == BookingStatus.CANCELLED;
	}

	public void cancel(Instant now, String cancellationReason) {
		if (!isCancelled()) {
			this.status = BookingStatus.CANCELLED;
			this.cancelledAt = now;
			this.cancellationReason = cancellationReason;
		}
	}
}
