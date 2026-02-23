package my.project.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "processed_messages")
public class MessageEntity {

    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column(nullable = false, length = 1000)
    private String jsonMessage;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MessageEntity(UUID messageId, String jsonMessage) {
        this.messageId = messageId;
        this.jsonMessage = jsonMessage;
        this.status = MessageStatus.CREATED;
        this.attempts = 0;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void done() {
        status = MessageStatus.DONE;
        updatedAt = Instant.now();
    }

    public void fail() {
        status = MessageStatus.FAILED;
        attempts++;
    }

    public void processing() {
        status = MessageStatus.PROCESSING;
        attempts++;
    }


}
