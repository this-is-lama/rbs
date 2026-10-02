package my.project.restaurantservice.contact.service.command;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.contact.dto.ContactDto;
import my.project.restaurantservice.contact.util.ContactValueNormalizer;
import my.project.restaurantservice.contact.mapper.ContactMapper;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactWriteService {

	private final RestaurantRepositoryService restaurantRepositoryService;
	private final ContactMapper mapper;

	/**
	 * Заменяет список контактов. Неизменённые контакты остаются теми же записями (id не меняется),
	 * лишние удаляются, новые добавляются.
	 */
	@CacheEvict(cacheNames = "contactsByRestaurantId", key = "#restId")
	@Transactional
	public void update(UUID restId, List<ContactDto> dtos) {
		var restaurant = restaurantRepositoryService.getById(restId);
		// телефоны приводим к единому виду до сравнения, иначе "8 900..." и "+7900..." считались бы разными
		List<ContactDto> toAdd = new ArrayList<>(dtos.stream()
				.map(dto -> new ContactDto(dto.type(), ContactValueNormalizer.normalize(dto.type(), dto.value())))
				.distinct()
				.toList());

		for (var contact : new ArrayList<>(restaurant.getContacts())) {
			if (!toAdd.remove(mapper.toDto(contact))) {
				restaurant.removeContact(contact);
			}
		}

		mapper.toEntity(toAdd).forEach(restaurant::addContact);
	}
}
