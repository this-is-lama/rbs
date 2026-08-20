package my.project.bookingservice.service.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.dto.response.TableAvailabilitySlotResponse;
import my.project.bookingservice.entity.BookingEntity;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.service.BookingReadService;
import my.project.bookingservice.service.usecase.BookingTableAvailabilityUseCase;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingTableAvailabilityUseCaseImpl implements BookingTableAvailabilityUseCase {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Moscow");

	private final BookingReadService readService;


	@Override
	public TableAvailabilityResponse getPublicTableAvailability(UUID restId,
																UUID tableId,
																LocalDate date) {
		log.info("Получение публичной занятости стола, restaurantId={}, tableId={}, date={}",
				restId, tableId, date);
		var bookings = getBookings(restId, tableId, date);
		return buildResponse(restId, tableId, date, bookings);
	}

	private List<BookingEntity> getBookings(UUID restaurantId,
											UUID tableId,
											LocalDate date) {
		return readService.findAllByRestaurantIdAndTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
						restaurantId,
						tableId,
						BookingStatus.RESERVED,
						date.atStartOfDay(BUSINESS_ZONE).toInstant(),
						date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant()
		);
	}

	private TableAvailabilityResponse buildResponse(UUID restaurantId,
													UUID tableId,
													LocalDate date,
													List<BookingEntity> bookings) {
		List<TableAvailabilitySlotResponse> reservedSlots = bookings.stream()
				.map(booking -> new TableAvailabilitySlotResponse(
						booking.getStartAt(),
						booking.getEndAt()
				))
				.toList();

		return new TableAvailabilityResponse(restaurantId, tableId, date, reservedSlots);
	}

}
