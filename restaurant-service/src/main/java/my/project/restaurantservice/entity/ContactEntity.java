package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import my.project.restaurantservice.entity.enums.ContactType;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(
    name = "restaurant_contacts",
    indexes = @Index(
            name = "idx_restaurant_contacts_restaurant_id",
            columnList = "restaurant_id"
    )
)
public class ContactEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ContactType type;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
