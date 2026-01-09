package my.project.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String surname;

	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;

	@Column(length = 12)
	private String phone;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}

}
