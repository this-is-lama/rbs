package my.project.restaurantservice.workinghours.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import my.project.restaurantservice.workinghours.dto.WorkingHoursDto;
import my.project.restaurantservice.workinghours.dto.WorkingHoursUpdateRequest;
import my.project.restaurantservice.workinghours.service.query.WorkingHoursQueryService;
import my.project.restaurantservice.workinghours.validator.WorkingHoursValidator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class WorkingHoursCommandService {

	private final WorkingHoursWriteService writeService;
	private final WorkingHoursQueryService queryService;
	private final ManagerAccessService managerAccessService;
	private final WorkingHoursValidator validator;

	public List<WorkingHoursDto> update(UUID restId, WorkingHoursUpdateRequest request, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		validator.validate(request.workingHours());
		writeService.update(restId, request.workingHours());
		return queryService.findAllByRestaurantId(restId);
	}
}
