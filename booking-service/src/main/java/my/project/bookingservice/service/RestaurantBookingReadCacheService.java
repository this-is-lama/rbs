package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.response.BookingUserResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantBookingReadCacheService {
	private final BookingRepository repository;
	private final BookingMapper mapper;
	private final UserBriefCacheService userBriefCacheService;

	@Cacheable(cacheNames = "restaurantBookings", key = "#restaurantId")
	public List<ManagerBookingResponse> findRestaurantBookings(UUID restaurantId) {
		log.info("Загрузка списка бронирований ресторана из БД, restaurantId={}", restaurantId);

		List<BookingEntity> bookings = repository.findAllByRestaurantIdOrderByCreatedAtDesc(restaurantId);
		Set<UUID> userIds = bookings.stream()
				.map(BookingEntity::getUserId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, BookingUserResponse> usersById = userBriefCacheService.getUserBriefs(userIds);
		return bookings.stream()
				.map(booking -> mapper.toManagerResponse(booking, usersById.get(booking.getUserId())))
				.toList();
	}
}
