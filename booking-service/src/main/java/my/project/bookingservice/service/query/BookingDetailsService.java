package my.project.bookingservice.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.RestaurantGateway;
import my.project.bookingservice.client.UserGateway;
import my.project.bookingservice.dto.client.UserDto;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.ManagerBookingResponse;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.common.exception.ForbiddenException;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingDetailsService {

	private final BookingQueryService queryService;
	private final BookingMapper mapper;

	private final RestaurantGateway restaurantGateway;
	private final UserGateway userGateway;

	public BookingResponse findById(UUID id, Authentication auth) {
		var booking = AuthUtil.isUser(auth)
				? queryService.getByIdAndUserId(id, AuthUtil.id(auth))
				: queryService.getById(id);

		UUID restId = booking.restaurant().restaurantId();
		if (AuthUtil.isManager(auth) && !restaurantGateway.hasManagerAccess(restId)) {
			log.warn("Менеджеру запрещён доступ к бронированию, bookingId={}, restId={}", id, restId);
			throw new ForbiddenException("booking.forbidden.booking-access");
		}

		return booking;
	}

	public List<BookingResponse> findUserBookings(Authentication auth) {
		return queryService.findAllByUserId(AuthUtil.id(auth));
	}

	public List<ManagerBookingResponse> findAllByRestaurantId(UUID restId, Authentication auth) {
		if (AuthUtil.isUser(auth) || (AuthUtil.isManager(auth) && !restaurantGateway.hasManagerAccess(restId))) {
			log.warn("Доступ к списку бронирований ресторана запрещён, restId={}", restId);
			throw new ForbiddenException("booking.forbidden.restaurant-bookings");
		}

		List<BookingResponse> bookings = queryService.findAllByRestaurantId(restId);
		Set<UUID> userIds = bookings.stream()
				.map(BookingResponse::userId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		var usersMap = userGateway.getUsersByIds(userIds)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(
						UserDto::id,
						Function.identity(),
						(oldValue, newValue) -> newValue
				));

		return bookings.stream()
				.map(booking -> mapper.toManagerResponse(booking, usersMap.getOrDefault(booking.userId(), null)))
				.toList();
	}
}
