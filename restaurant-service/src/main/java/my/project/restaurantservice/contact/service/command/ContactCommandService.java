package my.project.restaurantservice.contact.service.command;

import lombok.RequiredArgsConstructor;
import my.project.common.logging.Loggable;
import my.project.restaurantservice.contact.dto.ContactDto;
import my.project.restaurantservice.contact.dto.ContactsUpdateRequest;
import my.project.restaurantservice.contact.service.query.ContactQueryService;
import my.project.restaurantservice.manager.service.query.ManagerAccessService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Loggable
@Service
@RequiredArgsConstructor
public class ContactCommandService {

	private final ContactWriteService writeService;
	private final ContactQueryService queryService;
	private final ManagerAccessService managerAccessService;

	public List<ContactDto> update(UUID restId, ContactsUpdateRequest request, Authentication auth) {
		managerAccessService.checkAccess(restId, auth);
		writeService.update(restId, request.contacts());
		return queryService.findAllByRestaurantId(restId);
	}
}
