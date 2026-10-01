package my.project.restaurantservice.restaurant.service.command;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.manager.consistency.outbox.service.OutboxManagerService;
import my.project.restaurantservice.manager.service.command.ManagerWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantOutboxService {

	private final OutboxManagerService outboxManagerService;
	private final RestaurantWriteService restaurantWriteService;
	private final ManagerWriteService managerWriteService;

	@Transactional
	public List<UUID> deleteRestaurantAndOutbox(UUID id) {
		List<UUID> managers = managerWriteService.deleteAllByRestaurantId(id);
		List<UUID> outboxEvents = managers.stream().map(outboxManagerService::save).toList();

		restaurantWriteService.deleteById(id);
		return outboxEvents;
	}

}
