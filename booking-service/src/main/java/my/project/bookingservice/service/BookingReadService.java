package my.project.bookingservice.service;

import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingReadService {

	BookingResponse findById(UUID id, Authentication auth);

	List<BookingResponse> findUserBookings(Authentication auth);

	List<ManagerBookingResponse> findAllByRestaurantId(UUID restId, Authentication auth);

	BookingEntity findByAuth(UUID id, Authentication auth);

	List<BookingEntity> findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(UUID restId, UUID tableId,
																															  BookingStatus status,
																															  Instant from, Instant to);
}
