package my.project.bookingservice.service.query;

import lombok.RequiredArgsConstructor;
import my.project.bookingservice.dto.response.BookingResponse;
import my.project.bookingservice.dto.response.TableAvailabilityResponse;
import my.project.bookingservice.dto.response.TableAvailabilitySlotResponse;
import my.project.bookingservice.entity.BookingStatus;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.bookingservice.repository.BookingRepositoryService;
import my.project.common.logging.Loggable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Moscow");

	private final BookingRepositoryService repositoryService;
	private final BookingMapper mapper;

	@Transactional(readOnly = true)
	public BookingResponse getById(UUID id) {
		return mapper.toResponse(repositoryService.getById(id));
	}

	@Transactional(readOnly = true)
	public BookingResponse getByIdAndUserId(UUID id, UUID userId) {
		return mapper.toResponse(repositoryService.getByIdAndUserId(id, userId));
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> findAllByUserId(UUID userId) {
		return mapper.toResponse(repositoryService.findAllByUserIdOrderByCreatedAtDesc(userId));
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> findAllByRestaurantId(UUID restId) {
		return mapper.toResponse(repositoryService.findAllByRestaurantIdOrderByCreatedAtDesc(restId));
	}

	@Loggable
	@Transactional(readOnly = true)
	public TableAvailabilityResponse getTableAvailability(UUID restId, UUID tableId, LocalDate date) {
		List<TableAvailabilitySlotResponse> reservedSlots = repositoryService.findTableSlotsBetween(
				restId,
				tableId,
				BookingStatus.RESERVED,
				date.atStartOfDay(BUSINESS_ZONE).toInstant(),
				date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant()
		);

		return new TableAvailabilityResponse(restId, tableId, date, reservedSlots);
	}
}
