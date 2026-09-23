package my.project.restaurantservice.table.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.table.dto.TableDto;
import my.project.restaurantservice.table.dto.TableLayoutUpdateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class TableCommandService {

	private final TableWriteService writeService;
	private final ManagerAccessService managerAccessService;

	public UUID save(TableDto dto, UUID restId, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		return writeService.save(dto, restId);
	}

	public List<UUID> saveAll(List<TableDto> dtos, UUID restId, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		return writeService.saveAll(dtos, restId);
	}

	public TableDto update(UUID restId, UUID id, TableDto dto, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		return writeService.update(restId, id, dto);
	}

	public List<TableDto> updateLayout(UUID restId, TableLayoutUpdateRequest req, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		return writeService.updateLayout(restId, req);
	}

	public void delete(UUID restId, UUID id, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		writeService.deleteByIdAndRestaurantId(id, restId);
	}

}
