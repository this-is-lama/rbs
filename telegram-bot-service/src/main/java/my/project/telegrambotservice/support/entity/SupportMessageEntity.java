package my.project.telegrambotservice.support.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Сообщение переписки по обращению. Хранит id сообщения в чате пользователя и в чате поддержки:
 * по второму бот понимает, к какому обращению относится ответ (Reply) сотрудника.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
		name = "support_messages",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_support_messages_support_chat_message",
				columnNames = {"support_chat_id", "support_message_id"}
		),
		indexes = @Index(name = "idx_support_messages_ticket_id_created_at", columnList = "ticket_id, created_at")
)
public class SupportMessageEntity {

	/** content_type служебных сообщений бота в чате поддержки. */
	public static final String CARD = "CARD";
	public static final String HEADER = "HEADER";
	public static final String ATTACHMENT = "ATTACHMENT";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false)
	private SupportTicketEntity ticket;

	@Enumerated(EnumType.STRING)
	@Column(name = "author", nullable = false, length = 20)
	private MessageAuthor author;

	/** TEXT, PHOTO, DOCUMENT... — см. {@code Message.ContentType}. */
	@Column(name = "content_type", nullable = false, length = 20)
	private String contentType;

	@Column(name = "text", columnDefinition = "TEXT")
	private String text;

	@Column(name = "user_message_id")
	private Long userMessageId;

	@Column(name = "support_chat_id")
	private Long supportChatId;

	@Column(name = "support_message_id")
	private Long supportMessageId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
