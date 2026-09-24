package my.project.restaurantservice.restaurant.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.manager.service.command.ManagerWriteService;
import my.project.restaurantservice.photo.service.command.PhotoWriteService;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.dto.contact.ContactDto;
import my.project.restaurantservice.restaurant.dto.workinghours.WorkingHoursDto;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.entity.WeekDay;
import my.project.restaurantservice.restaurant.mapper.ContactMapper;
import my.project.restaurantservice.restaurant.mapper.RestaurantMapper;
import my.project.restaurantservice.restaurant.mapper.WorkingHoursMapper;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantWriteService {

	private final RestaurantRepositoryService repositoryService;
	private final ManagerWriteService managerWriteService;
	private final PhotoWriteService photoWriteService;

	private final RestaurantMapper mapper;
	private final ContactMapper contactMapper;
	private final WorkingHoursMapper workingHoursMapper;

	@Transactional
	public UUID save(RestaurantDto dto, UUID managerId) {
		RestaurantEntity restaurant = mapper.toEntity(dto);

		contactMapper.toEntity(dto.getContacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.getWorkingHours()).forEach(restaurant::addWorkingHours);

		UUID restId = repositoryService.save(restaurant).getId();

		if (managerId != null) {
			managerWriteService.save(restId, managerId);
			log.info("Текущий менеджер привязан к ресторану, restId={}, managerId={}", restId, managerId);
		}

		return restId;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id"),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id")
	})
	@Transactional
	public void update(UUID id, RestaurantDto dto) {
		var restaurant = repositoryService.getById(id);

		mapper.updateEntity(restaurant, dto);
		syncWorkingHours(restaurant, dto.getWorkingHours());
		syncContacts(restaurant, dto.getContacts());
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id", beforeInvocation = true)
	})
	@Transactional
	public List<UUID> deleteById(UUID id) {
		photoWriteService.markDeletingByRestaurantId(id);
		List<UUID> managerIds = managerWriteService.deleteAllByRestaurantId(id);
		repositoryService.deleteById(id);
		return managerIds;
	}

	private void syncWorkingHours(RestaurantEntity restaurant, List<WorkingHoursDto> dtos) {
		Map<WeekDay, WorkingHoursDto> byDay = dtos.stream()
				.collect(Collectors.toMap(WorkingHoursDto::dayOfWeek, Function.identity()));

		for (var wh : new ArrayList<>(restaurant.getWorkingHours())) {
			var dto = byDay.remove(wh.getDayOfWeek());
			if (dto == null) {
				restaurant.removeWorkingHours(wh);
			} else {
				workingHoursMapper.updateEntity(wh, dto);
			}
		}

		byDay.values().stream()
				.map(workingHoursMapper::toEntity)
				.forEach(restaurant::addWorkingHours);
	}

	private void syncContacts(RestaurantEntity restaurant, List<ContactDto> dtos) {
		List<ContactDto> toAdd = new ArrayList<>(dtos);

		for (var contact : new ArrayList<>(restaurant.getContacts())) {
			if (!toAdd.remove(contactMapper.toDto(contact))) {
				restaurant.removeContact(contact);
			}
		}

		contactMapper.toEntity(toAdd).forEach(restaurant::addContact);
	}
}
