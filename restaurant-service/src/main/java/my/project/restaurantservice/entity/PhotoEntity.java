package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import my.project.restaurantservice.entity.enums.PhotoCategory;
import my.project.restaurantservice.entity.enums.PhotoStatus;
import my.project.restaurantservice.service.photo.provider.ContainerType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "photos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_photos_object_key",
                columnNames = "object_key"
        ),
        indexes = {
                @Index(
                        name = "idx_photos_restaurant_status_sort",
                        columnList = "restaurant_id, status, sort_order"
                ),
                @Index(
                        name = "idx_photos_dish_status_sort",
                        columnList = "dish_id, status, sort_order"
                ),
                @Index(
                        name = "idx_photos_status_uploaded_at",
                        columnList = "status, uploaded_at"
                )
        }
)
public class PhotoEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Setter
    @Column(name = "bucket", nullable = false, length = 64)
    private String bucket;

    @Setter
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Setter
    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32)
    private PhotoCategory category;

    @Setter
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PhotoStatus status;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private RestaurantEntity restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private DishEntity dish;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) uploadedAt = Instant.now();
        if (status == null) status = PhotoStatus.PENDING;
    }

    // вместо публичных setDish/setRestaurant
    public void assignRestaurant(RestaurantEntity restaurant) {
        this.restaurant = restaurant;
        this.dish = null;
    }

    public void assignDish(DishEntity dish) {
        this.dish = dish;
        this.restaurant = null;
    }

    public void confirm() {
        this.status = PhotoStatus.ACTIVE;
        this.confirmedAt = Instant.now();
    }

    public void expired() {
        this.status = PhotoStatus.EXPIRED;
    }

    public void deleting() {
        this.status = PhotoStatus.DELETING;
    }

    public boolean isOwnContainerAndBucket(ContainerType type, UUID containerId, String expectedBucket) {
        if (!Objects.equals(bucket, expectedBucket)) {
            return false;
        }
        return switch (type) {
            case RESTAURANTS -> restaurant != null && Objects.equals(restaurant.getId(), containerId);
            case DISHES -> dish != null && Objects.equals(dish.getId(), containerId);
        };
    }

}
