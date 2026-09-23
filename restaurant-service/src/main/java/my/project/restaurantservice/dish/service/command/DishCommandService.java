package my.project.restaurantservice.dish.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.dish.dto.DishDto;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class DishCommandService {

	private final DishWriteService writeService;
	private final ManagerAccessService managerAccessService;

	public UUID save(DishDto dto, UUID restId, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		return writeService.save(dto, restId);
	}

	public DishDto update(UUID restId, UUID id, DishDto dto, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		return writeService.update(restId, id, dto);
	}

	public void delete(UUID restId, UUID id, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		writeService.deleteByIdAndRestaurantId(id, restId);
	}

}
