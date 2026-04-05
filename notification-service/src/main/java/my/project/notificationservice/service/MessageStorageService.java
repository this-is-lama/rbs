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
        try {
            MessageEntity message = new MessageEntity(event.bookingId(), mapper.writeJson(event));
            repository.save(message);

            log.info("Сообщение сохранено в хранилище, bookingId={}", event.bookingId());
            return true;
        } catch (DataIntegrityViolationException ignored) {
            log.info("Сообщение уже было сохранено ранее, bookingId={}", event.bookingId());
            return false;
        }
    }

    @Transactional
    public void markStatus(UUID bookingId, Consumer<MessageEntity> action) {
        repository.findById(bookingId).ifPresent(m -> {
            MessageStatus oldStatus = m.getStatus();

            action.accept(m);
            repository.save(m);

            log.info("Статус сообщения обновлён, bookingId={}, oldStatus={}, newStatus={}, attempts={}",
                    bookingId, oldStatus, m.getStatus(), m.getAttempts());
        });
    }

    @Transactional
    public List<MessageEntity> getWorkBatch(int maxAttempts, int stuckMinutes) {
        var batch = repository.lockWorkBatch(maxAttempts, stuckMinutes);
        batch.forEach(MessageEntity::processing);

        var savedBatch = repository.saveAll(batch);

        if (!savedBatch.isEmpty()) {
            log.info("Получена рабочая пачка сообщений для повторной обработки, count={}", savedBatch.size());
        }

        return savedBatch;
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