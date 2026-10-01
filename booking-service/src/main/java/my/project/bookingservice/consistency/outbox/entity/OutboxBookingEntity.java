package my.project.bookingservice.consistency.outbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox")
public class OutboxBookingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 50)
	private OutboxEventType eventType;

	@Column(name = "booking_id", nullable = false, columnDefinition = "uuid")
	private UUID bookingId;

	@Column(name = "payload", nullable = false, columnDefinition = "text")
	private String payload;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Column(name = "last_error", length = 1000)
	private String lastError;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
