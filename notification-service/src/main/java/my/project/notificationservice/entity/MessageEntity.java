package my.project.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "processed_messages",
        indexes = {
                @Index(
                        name = "idx_processed_messages_status_updated_at",
                        columnList = "status, updated_at"
                ),
                @Index(
                        name = "idx_processed_messages_status_attempts_updated_at",
                        columnList = "status, attempts, updated_at"
                ),
                @Index(
                        name = "idx_processed_messages_type_created_at",
                        columnList = "message_type, created_at"
                )
        }
)
public class MessageEntity {

    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 50)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Lob
    @Column(name = "json_message", nullable = false, columnDefinition = "text")
    private String jsonMessage;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MessageEntity(UUID messageId, MessageType messageType, String jsonMessage) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.jsonMessage = jsonMessage;
        this.status = MessageStatus.CREATED;
        this.attempts = 0;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public void done() {
        status = MessageStatus.DONE;
        updatedAt = Instant.now();
    }

    public void fail() {
        status = MessageStatus.FAILED;
        updatedAt = Instant.now();
    }

    public void processing() {
        status = MessageStatus.PROCESSING;
        attempts++;
        updatedAt = Instant.now();
    }
}