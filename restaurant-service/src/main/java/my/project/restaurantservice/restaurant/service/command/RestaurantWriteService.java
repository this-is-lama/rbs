package my.project.restaurantservice.restaurant.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ConflictException;
import my.project.restaurantservice.contact.mapper.ContactMapper;
import my.project.restaurantservice.manager.service.command.ManagerWriteService;
import my.project.restaurantservice.photo.service.command.PhotoWriteService;
import my.project.restaurantservice.restaurant.dto.RestaurantCreateRequest;
import my.project.restaurantservice.restaurant.dto.RestaurantUpdateDto;
import my.project.restaurantservice.restaurant.entity.RestaurantEntity;
import my.project.restaurantservice.restaurant.entity.RestaurantStatus;
import my.project.restaurantservice.restaurant.mapper.RestaurantMapper;
import my.project.restaurantservice.restaurant.repository.RestaurantRepositoryService;
import my.project.restaurantservice.workinghours.mapper.WorkingHoursMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
	public UUID save(RestaurantCreateRequest dto, UUID managerId) {
		RestaurantEntity restaurant = mapper.toEntity(dto);
		restaurant.setStatus(RestaurantStatus.DRAFT);

		contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);

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
	public void update(UUID id, RestaurantUpdateDto dto) {
		var restaurant = repositoryService.getById(id);
		mapper.updateEntity(restaurant, dto);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id"),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id")
	})
	@Transactional
	public void changeActive(UUID id, boolean active) {
		var restaurant = repositoryService.getById(id);
		if (restaurant.getStatus() != RestaurantStatus.ACTIVE && restaurant.getStatus() != RestaurantStatus.INACTIVE) {
			log.warn("Смена активности отклонена: ресторан не верифицирован, restId={}, status={}",
					id, restaurant.getStatus());
			throw new ConflictException("restaurant.status.change-not-allowed", id);
		}

		restaurant.setStatus(active ? RestaurantStatus.ACTIVE : RestaurantStatus.INACTIVE);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "publicRestaurantById", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "privateRestaurantById", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "contactsByRestaurantId", key = "#id", beforeInvocation = true),
			@CacheEvict(cacheNames = "workingHoursByRestaurantId", key = "#id", beforeInvocation = true)
	})
	@Transactional
	public void deleteById(UUID id) {
		photoWriteService.markDeletingByRestaurantId(id);
		repositoryService.deleteById(id);
	}
}
