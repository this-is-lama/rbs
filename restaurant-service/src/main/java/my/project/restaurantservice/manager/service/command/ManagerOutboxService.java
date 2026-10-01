package my.project.restaurantservice.manager.service.command;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.manager.consistency.outbox.service.OutboxManagerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerOutboxService {

	private final ManagerWriteService managerWriteService;
	private final OutboxManagerService outboxWriteService;

	@Transactional
	public UUID saveManagerAndOutbox(UUID restId, UUID managerId) {
		managerWriteService.save(restId, managerId);
		return outboxWriteService.save(managerId);
	}

	@Transactional
	public UUID deleteManagerAndOutbox(UUID restId, UUID managerId) {
		managerWriteService.deleteManager(restId, managerId);
		return outboxWriteService.save(managerId);
	}

}
