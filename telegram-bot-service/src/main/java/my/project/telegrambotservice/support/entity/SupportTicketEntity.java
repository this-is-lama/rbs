package my.project.telegrambotservice.support.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Обращение в поддержку. Номер обращения, который видит пользователь (#12), — это {@code id}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
		name = "support_tickets",
		indexes = {
				@Index(name = "idx_support_tickets_chat_id_created_at", columnList = "chat_id, created_at"),
				@Index(name = "idx_support_tickets_status_updated_at", columnList = "status, updated_at")
		}
)
public class SupportTicketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Личный чат пользователя с ботом (совпадает с его Telegram id). */
	@Column(name = "chat_id", nullable = false)
	private long chatId;

	@Column(name = "user_display_name", nullable = false)
	private String userDisplayName;

	@Column(name = "username", length = 64)
	private String username;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, length = 30)
	private TicketCategory category;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private TicketStatus status;

	@Column(name = "description", nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(name = "restaurant", length = 500)
	private String restaurant;

	@Column(name = "email")
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "rating", length = 20)
	private TicketRating rating;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "closed_at")
	private Instant closedAt;

	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (status == null) {
			status = TicketStatus.OPEN;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = Instant.now();
	}

	public void markOpen() {
		status = TicketStatus.OPEN;
		closedAt = null;
	}

	public void markAnswered() {
		status = TicketStatus.ANSWERED;
		closedAt = null;
	}

	public void close() {
		status = TicketStatus.CLOSED;
		closedAt = Instant.now();
	}
}
