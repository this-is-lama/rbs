package my.project.restaurantservice.contact.repository;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.contact.entity.ContactEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactRepositoryService {

	private final ContactRepository repository;

	@Transactional(readOnly = true)
	public List<ContactEntity> findAllByRestaurantId(UUID restId) {
		return repository.findAllByRestaurantIdOrderByCreatedAtAsc(restId);
	}
}
