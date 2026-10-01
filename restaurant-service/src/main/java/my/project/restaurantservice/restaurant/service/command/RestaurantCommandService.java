package my.project.restaurantservice.restaurant.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ConflictException;
import my.project.common.logging.Loggable;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.internal.client.BookingGateway;
import my.project.restaurantservice.internal.dto.BookingStatus;
import my.project.restaurantservice.manager.consistency.outbox.service.OutboxProcessorService;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.service.query.RestaurantQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantCommandService {

	private final RestaurantWriteService writeService;
	private final RestaurantQueryService queryService;
	private final ManagerAccessService managerAccessService;
	private final RestaurantOutboxService restaurantOutboxService;
	private final OutboxProcessorService outboxProcessorService;
	private final BookingGateway bookingGateway;

	public UUID create(RestaurantDto dto, Authentication auth) {
		UUID managerId = AuthUtil.isManager(auth) ? AuthUtil.id(auth) : null;
		return writeService.save(dto, managerId);
	}

	public RestaurantDto update(UUID id, RestaurantDto dto, Authentication auth) {
		managerAccessService.checkAccess(id, auth);
		writeService.update(id, dto);
		return queryService.getPrivateById(id);
	}

	public void changeActive(UUID id, boolean active, Authentication auth) {
		managerAccessService.checkAccess(id, auth);
		writeService.changeActive(id, active);
	}

	public void delete(UUID id, Authentication auth) {
		managerAccessService.checkAccess(id, auth);
		checkDisabled(id);
		checkNoActiveBookings(id);
		List<UUID> outboxEvents = restaurantOutboxService.deleteRestaurantAndOutbox(id);
		for (UUID event : outboxEvents) {
			outboxProcessorService.process(event);
		}
	}

	private void checkDisabled(UUID id) {
		if (queryService.isActive(id)) {
			log.warn("Удаление ресторана отклонено: ресторан не отключён, restId={}", id);
			throw new ConflictException("restaurant.must-be-disabled", id);
		}
	}

	private void checkNoActiveBookings(UUID id) {
		Instant now = Instant.now();
		boolean hasActiveBookings = bookingGateway.getRestaurantBookings(id).stream()
				.anyMatch(b -> b.status() == BookingStatus.RESERVED && b.endAt().isAfter(now));

		if (hasActiveBookings) {
			log.warn("Удаление ресторана отклонено: есть активные бронирования, restId={}", id);
			throw new ConflictException("restaurant.has-active-bookings", id);
		}
	}
}
