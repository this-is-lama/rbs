package my.project.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(
        name = "restaurants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restaurant_name_restaurant_address",
                columnNames = {
                        "name",
                        "address"
                }
        )
)
public class RestaurantEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkingHoursEntity> workingHours = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContactEntity> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantPhotoEntity> photos = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TableEntity> tables = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DishEntity> dishes = new ArrayList<>();


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

    public void addWorkingHours(WorkingHoursEntity wh) {
        workingHours.add(wh);
        wh.setRestaurant(this);
    }
    public void removeWorkingHours(WorkingHoursEntity wh) {
        workingHours.remove(wh);
        wh.setRestaurant(null);
    }

    public void addContact(ContactEntity contact) {
        contacts.add(contact);
        contact.setRestaurant(this);
    }

    public void removeContact(ContactEntity contact) {
        contacts.remove(contact);
        contact.setRestaurant(null);
    }

    public void addPhoto(RestaurantPhotoEntity photo) {
        photos.add(photo);
        photo.setRestaurant(this);
    }

    public void removePhoto(RestaurantPhotoEntity photo) {
        photos.remove(photo);
        photo.setRestaurant(null);
    }

    public void addTable(TableEntity table) {
        tables.add(table);
        table.setRestaurant(this);
    }

    public void removeTable(TableEntity table) {
        tables.remove(table);
        table.setRestaurant(null);
    }

    public void addDish(DishEntity dish) {
        dishes.add(dish);
        dish.setRestaurant(this);
    }

    public void removeDish(DishEntity dish) {
        dishes.remove(dish);
        dish.setRestaurant(null);
    }
}
