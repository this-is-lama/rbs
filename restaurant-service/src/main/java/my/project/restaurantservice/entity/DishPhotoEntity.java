package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
        name = "dish_photos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dish_photos_object_key",
                columnNames = "object_key"
        ),
        indexes = @Index(
                name = "idx_dish_photos_dish_id",
                columnList = "dish_id"
        )
)
public class DishPhotoEntity extends PhotoEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    private DishEntity dish;

}
