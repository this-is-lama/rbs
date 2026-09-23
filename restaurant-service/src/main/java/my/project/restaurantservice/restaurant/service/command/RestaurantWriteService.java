package my.project.restaurantservice.restaurant.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.manager.service.command.ManagerWriteService;
import my.project.restaurantservice.restaurant.dto.RestaurantDto;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.mapper.ContactMapper;
import my.project.restaurantservice.restaurant.mapper.RestaurantMapper;
import my.project.restaurantservice.restaurant.mapper.WorkingHoursMapper;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantWriteService {

	private final RestaurantRepositoryService repositoryService;
	private final ManagerWriteService managerWriteService;

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

		for (var c : new ArrayList<>(restaurant.getContacts())) {
			restaurant.removeContact(c);
		}
		for (var wh : new ArrayList<>(restaurant.getWorkingHours())) {
			restaurant.removeWorkingHours(wh);
		}

		repositoryService.flush();

		contactMapper.toEntity(dto.getContacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.getWorkingHours()).forEach(restaurant::addWorkingHours);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id", beforeInvocation = true)
	})
	@Transactional
	public void deleteById(UUID id) {
		repositoryService.deleteById(id);
	}
}
