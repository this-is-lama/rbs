package my.project.bookingservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.common.exception.ConflictException;
import my.project.common.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingRepositoryService {

	private final BookingRepository repository;

	@Transactional(readOnly = true)
	public BookingEntity getById(UUID id) {
		return repository.findById(id).orElseThrow(() -> {
			log.warn("Бронирование не найдено, bookingId={}", id);
			return new NotFoundException("booking.not-found", id);
		});
	}

	@Transactional(readOnly = true)
	public BookingEntity getByIdAndUserId(UUID id, UUID userId) {
		return repository.findByIdAndUserId(id, userId).orElseThrow(() -> {
			log.warn("Бронирование не найдено, bookingId={}", id);
			return new NotFoundException("booking.not-found", id);
		});
	}

	@Transactional(readOnly = true)
	public List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId) {
		return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
	}

	@Transactional(readOnly = true)
	public List<BookingEntity> findAllByRestaurantIdOrderByCreatedAtDesc(UUID restId) {
		return repository.findAllByRestaurantIdOrderByCreatedAtDesc(restId);
	}

	@Transactional(readOnly = true)
	public List<BookingEntity> findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(UUID restId, UUID tableId,
																															  BookingStatus status,
																															  Instant from, Instant to) {
		return repository.findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(restId, tableId, status, from, to);
	}

	@Transactional
	public BookingEntity save(BookingEntity booking) {
		try {
			return repository.saveAndFlush(booking);
		} catch (DataIntegrityViolationException e) {
			log.warn("Конфликт при сохранении бронирования: пересечение по времени, tableId={}", booking.getTableId(), e);
			throw new ConflictException("booking.overlap");
		}
	}
}
