package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "restaurant_managers",
        uniqueConstraints = @UniqueConstraint(
            name = "uk_restaurant_manager",
            columnNames = {"restaurant_id", "manager_id"}
        ),
        indexes = {
            @Index(name = "idx_rm_restaurant", columnList = "restaurant_id"),
            @Index(name = "idx_rm_manager", columnList = "manager_id")
        }
)
public class ManagerEntity {

    @EmbeddedId
    private ManagerId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public UUID getRestaurantId() {
        return id.getRestaurantId();
    }

    public UUID getManagerId() {
        return id.getManagerId();
    }
}
