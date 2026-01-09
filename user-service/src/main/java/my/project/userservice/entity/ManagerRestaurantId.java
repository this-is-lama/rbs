package my.project.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ManagerRestaurantId implements Serializable {

	@Column(name = "manager_id", columnDefinition = "uuid")
	private UUID managerId;

	@Column(name = "restaurant_id", columnDefinition = "uuid")
	private UUID restaurantId;

}
