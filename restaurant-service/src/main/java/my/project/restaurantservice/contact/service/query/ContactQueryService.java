package my.project.restaurantservice.contact.service.query;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.contact.dto.ContactDto;
import my.project.restaurantservice.contact.mapper.ContactMapper;
import my.project.restaurantservice.contact.repository.ContactRepositoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactQueryService {

	private final ContactRepositoryService repositoryService;
	private final ContactMapper mapper;

	@Cacheable(cacheNames = "contactsByRestaurantId", key = "#restId", sync = true)
	public List<ContactDto> findAllByRestaurantId(UUID restId) {
		return mapper.toDto(repositoryService.findAllByRestaurantId(restId));
	}
}
