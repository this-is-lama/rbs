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
import java.util.stream.Collectors;

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
            return true;
        } catch (DataIntegrityViolationException ignored) {
            return false;
        }
    }

    @Transactional
    public void markStatus(UUID bookingId, Consumer<MessageEntity> action) {
        repository.findById(bookingId).ifPresent(m -> {
            action.accept(m);
            repository.save(m);
        });
    }

    @Transactional
    public List<MessageEntity> getWorkBatch(int maxAttempts, int stuckMinutes) {
        var batch = repository.lockWorkBatch(maxAttempts, stuckMinutes);
        batch.forEach(MessageEntity::processing);

        return repository.saveAll(batch);
    }

    @Transactional
    public long cleanDone(Instant time) {
        return repository.deleteTop500ByStatusAndUpdatedAtLessThan(MessageStatus.DONE, time);
    }

}
