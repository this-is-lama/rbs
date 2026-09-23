package my.project.restaurantservice.table.service.query;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.table.dto.TableDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class TableDetailsService {

	private final TableQueryService queryService;
	private final ManagerAccessService managerAccessService;

	public TableDto findById(UUID restId, UUID id, Authentication auth) {
		return managerAccessService.onlyPublicAccess(restId, auth)
				? queryService.getPublicById(restId, id)
				: queryService.getPrivateById(restId, id);
	}

	public List<TableDto> findAllByRestaurantId(UUID restId, Authentication auth) {
		return managerAccessService.onlyPublicAccess(restId, auth)
				? queryService.findAllPublicByRestaurantId(restId)
				: queryService.findAllPrivateByRestaurantId(restId);
	}

}