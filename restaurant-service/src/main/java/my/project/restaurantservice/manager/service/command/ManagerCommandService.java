package my.project.restaurantservice.manager.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.manager.consistency.outbox.service.OutboxProcessorService;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class ManagerCommandService {

	private final ManagerAccessService accessService;

	private final ManagerOutboxService outboxService;
	private final OutboxProcessorService processorService;

	public UUID addManager(UUID restId, UUID managerId, Authentication auth) {
		accessService.checkAccess(restId, auth);

		UUID outboxId = outboxService.saveManagerAndOutbox(restId, managerId);

		processorService.process(outboxId);

		return managerId;
	}

	public void deleteManager(UUID restId, UUID managerId, Authentication auth) {
		accessService.checkAccess(restId, auth);

		UUID outboxId = outboxService.deleteManagerAndOutbox(restId, managerId);
		processorService.process(outboxId);
	}

}