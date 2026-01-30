package my.project.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
		name = "refresh_tokens",
		indexes = {
				@Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
		}
)
public class RefreshTokenEntity {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "jti_hash", unique = true, nullable = false)
	private String jtiHash;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	//истекает
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public void deactivate() {
		this.active = false;
	}

	public boolean isExpired(Instant now) {
		return !expiresAt.isAfter(now);
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			this.createdAt = Instant.now();
		}
		if (active == null) {
			this.active = true;
		}
	}

}
