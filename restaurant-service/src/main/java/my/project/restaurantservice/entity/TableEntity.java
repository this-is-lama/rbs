package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "restaurant_tables",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restaurant_tables_restaurant_id_table_number",
                columnNames = {
                        "restaurant_id",
                        "table_number"
                }
        ),
        indexes = {
                @Index(
                        name = "idx_restaurant_tables_restaurant_id",
                        columnList = "restaurant_id"
                ),
                @Index(
                        name = "idx_restaurant_tables_restaurant_active",
                        columnList = "restaurant_id, is_active"
                )
        }
)
public class TableEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "marker_size")
    private Integer markerSize;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
