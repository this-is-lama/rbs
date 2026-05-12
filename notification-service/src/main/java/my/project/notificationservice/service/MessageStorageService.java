package my.project.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.entity.MessageStatus;
import my.project.notificationservice.events.BookingNotificationEvent;
import my.project.notificationservice.mapper.MessageJsonMapper;
import my.project.notificationservice.repository.MessageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageStorageService {

    private final MessageRepository repository;
    private final MessageJsonMapper mapper;

    public UUID messageId(BookingNotificationEvent event) {
        String value = event.messageType().name() + ":" + event.bookingId();
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public boolean save(BookingNotificationEvent event) {
        UUID messageId = messageId(event);

        if (repository.existsById(messageId)) {
            log.info("Сообщение уже было сохранено ранее, messageId={}, bookingId={}, messageType={}",
                    messageId, event.bookingId(), event.messageType());
            return false;
        }

        try {
            MessageEntity message = new MessageEntity(messageId, event.messageType(), mapper.writeJson(event));
            repository.saveAndFlush(message);

            log.info("Сообщение сохранено в хранилище, messageId={}, bookingId={}, messageType={}",
                    messageId, event.bookingId(), event.messageType());
            return true;
        } catch (DataIntegrityViolationException e) {
            log.info("Сообщение уже было сохранено ранее, messageId={}, bookingId={}, messageType={}",
                    messageId, event.bookingId(), event.messageType());
            return false;
        }
    }

    @Transactional
    public void markStatus(UUID messageId, Consumer<MessageEntity> action) {
        repository.findById(messageId).ifPresentOrElse(message -> {
            MessageStatus oldStatus = message.getStatus();

            action.accept(message);

            log.info("Статус сообщения обновлён, messageId={}, oldStatus={}, newStatus={}, attempts={}",
                    messageId, oldStatus, message.getStatus(), message.getAttempts());
        }, () -> log.warn("Сообщение не найдено для обновления статуса, messageId={}", messageId));
    }

    @Transactional
    public List<MessageEntity> getWorkBatch(int maxAttempts, int stuckMinutes) {
        var batch = repository.lockWorkBatch(maxAttempts, stuckMinutes);

        batch.forEach(MessageEntity::processing);

        if (!batch.isEmpty()) {
            log.info("Получена рабочая пачка сообщений для повторной обработки, count={}", batch.size());
        }

        return batch;
    }

    @Transactional
    public long cleanDone(Instant time) {
        long deleted = repository.deleteTop500ByStatusAndUpdatedAtLessThan(MessageStatus.DONE, time);

        if (deleted > 0) {
            log.info("Удалены обработанные сообщения, deleted={}, olderThan={}", deleted, time);
        }

        return deleted;
    }
}