package my.project.bookingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.repository.BookingRepository;
import my.project.bookingservice.service.BookingPersistenceService;
import my.project.common.exception.ConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingPersistenceServiceImpl implements BookingPersistenceService {

	private final BookingRepository repository;

	@Transactional
	public BookingEntity save(BookingEntity booking) {
		try {
			BookingEntity saved = repository.saveAndFlush(booking);
			log.info("Бронирование сохранено в базе данных, bookingId={}", saved.getId());
			return saved;
		} catch (DataIntegrityViolationException e) {
			log.warn("Конфликт при сохранении бронирования: пересечение по времени, tableId={}", booking.getTableId(), e);
			throw new ConflictException("booking.overlap");
		}
	}

	@Transactional
	public BookingEntity cancel(BookingEntity booking, String reason) {
		Instant now = Instant.now();
		booking.cancel(now, reason);
		return repository.saveAndFlush(booking);
	}

}