package my.project.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.notificationservice.entity.MessageEntity;
import my.project.notificationservice.entity.MessageStatus;
import my.project.notificationservice.events.BookingCreatedEvent;
import my.project.notificationservice.mapper.MessageJsonMapper;
import my.project.notificationservice.repository.MessageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public boolean save(BookingCreatedEvent event) {
        UUID bookingId = event.bookingId();

        if (repository.existsById(bookingId)) {
            log.info("Сообщение уже было сохранено ранее, bookingId={}", bookingId);
            return false;
        }

        try {
            MessageEntity message = new MessageEntity(bookingId, mapper.writeJson(event));
            repository.saveAndFlush(message);

            log.info("Сообщение сохранено в хранилище, bookingId={}", bookingId);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.info("Сообщение уже было сохранено ранее, bookingId={}", bookingId);
            return false;
        }
    }

    @Transactional
    public void markStatus(UUID bookingId, Consumer<MessageEntity> action) {
        repository.findById(bookingId).ifPresentOrElse(message -> {
            MessageStatus oldStatus = message.getStatus();

            action.accept(message);

            log.info("Статус сообщения обновлён, bookingId={}, oldStatus={}, newStatus={}, attempts={}",
                    bookingId, oldStatus, message.getStatus(), message.getAttempts());
        }, () -> log.warn("Сообщение не найдено для обновления статуса, bookingId={}", bookingId));
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