package my.project.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table
public class ManagerRestaurantEntity {

	@EmbeddedId
	private ManagerRestaurantId id;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		createdAt = Instant.now();
	}
}
