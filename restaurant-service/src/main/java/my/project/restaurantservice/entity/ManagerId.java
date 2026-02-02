package my.project.restaurantservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ManagerId implements Serializable {

    @Column(name = "restaurant_id", columnDefinition = "uuid")
    private UUID restaurantId;

    @Column(name = "manager_id", columnDefinition = "uuid")
    private UUID managerId;
}
