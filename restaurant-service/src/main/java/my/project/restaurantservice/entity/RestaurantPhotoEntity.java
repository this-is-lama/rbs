package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "restaurant_photos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restaurant_photos_object_key",
                columnNames = "object_key"
        ),
        indexes = @Index(
                name = "idx_restaurant_photos_restaurant_id",
                columnList = "restaurant_id"
        )
)
public class RestaurantPhotoEntity extends PhotoEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PhotoCategory category;

}

