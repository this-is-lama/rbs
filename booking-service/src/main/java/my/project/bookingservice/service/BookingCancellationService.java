package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.request.CancelBookingRequest;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.pricing.history.PricingHistoryAggregateCacheEvictService;
import my.project.bookingservice.repository.BookingRepository;
import my.project.common.exception.ValidationException;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCancellationService {
	private final BookingReadService readService;
	private final BookingRepository bookingRepository;
	private final BookingAvailabilityCacheService availabilityCacheService;
	private final PricingHistoryAggregateCacheEvictService historyAggregateCacheEvictService;
	private final RestaurantBookingCacheEvictService restaurantBookingCacheEvictService;
	private final BookingEventService eventService;

	public void cancel(UUID bookingId, CancelBookingRequest request, Authentication auth) {
		Instant now = Instant.now();
		log.info("Отмена бронирования, bookingId={}", bookingId);

		BookingEntity booking = readService.findByAuth(bookingId, auth);

		if (booking.isCancelled()) {
			log.info("Бронирование уже было отменено ранее, bookingId={}", bookingId);
			return;
		}

		boolean cancelledByManagerOrAdmin = AuthUtil.isManager(auth) || AuthUtil.isAdmin(auth);
		String reason = request == null ? null : request.reason();

		if (cancelledByManagerOrAdmin && (reason == null || reason.isBlank())) {
			log.warn("Причина отмены бронирования менеджером или администратором не указана, bookingId={}", bookingId);
			throw new ValidationException("booking.cancel.reason-required");
		}

		booking.cancel(now, reason);
		save(booking);
		evictAvailabilityCache(booking);
		historyAggregateCacheEvictService.evict(booking.getRestaurantId());
		restaurantBookingCacheEvictService.evict(booking.getRestaurantId());

		log.info("Бронирование помечено как отменённое, bookingId={}", bookingId);

		if (cancelledByManagerOrAdmin) {
			eventService.sendCancelled(booking, reason);
		}
	}

	private BookingEntity save(BookingEntity booking) {
		BookingEntity saved = bookingRepository.saveAndFlush(booking);
		log.info("Отмена бронирования сохранена в базе данных, bookingId={}", saved.getId());
		return saved;
	}

	private void evictAvailabilityCache(BookingEntity booking) {
		availabilityCacheService.evict(
				booking.getRestaurantId(),
				booking.getTableId(),
				BookingTimeUtils.businessDate(booking.getStartAt())
		);
	}
}
