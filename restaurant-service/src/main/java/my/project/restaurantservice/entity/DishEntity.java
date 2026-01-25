package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(
    name = "dishes",
    indexes = @Index(
            name = "idx_dishes_restaurant_id",
            columnList = "restaurant_id"
    )
)
public class DishEntity implements PhotoContainer {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "dish",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder ASC")
    private List<PhotoEntity> photos = new ArrayList<>();


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

    @Override
    public void addPhoto(PhotoEntity photo) {
        photos.add(photo);
        photo.assignDish(this);
    }

    @Override
    public void removePhoto(PhotoEntity photo) {
        photos.remove(photo);
        photo.assignDish(null);
    }

}
