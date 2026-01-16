package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "restaurant_working_hours",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_restaurant_working_hours_restaurant_day",
        columnNames = { "restaurant_id", "day_of_week" }
    ),
    indexes = @Index(
        name = "idx_working_hours_restaurant_id",
        columnList = "restaurant_id"
    )
)
public class WorkingHoursEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private WeekDay dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;   // NULL = закрыто

    @Column(name = "close_time")
    private LocalTime closeTime;  // NULL = закрыто

    @Column(name = "is_closed", nullable = false)
    private boolean closed;
}
