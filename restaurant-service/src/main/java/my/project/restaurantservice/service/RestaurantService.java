package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.security.AuthUtil;
import my.project.common.exception.NotFoundException;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantInfoDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.mapper.ContactMapper;
import my.project.restaurantservice.mapper.RestaurantMapper;
import my.project.restaurantservice.mapper.WorkingHoursMapper;
import my.project.restaurantservice.repository.RestaurantRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private final RestaurantMapper restaurantMapper;
	private final ContactMapper contactMapper;
	private final WorkingHoursMapper workingHoursMapper;

	private final RestaurantRepository repository;
	private final ManagerService managerService;

	@Transactional
	public UUID save(RestaurantDto dto, Authentication auth) {
		RestaurantEntity restaurant = restaurantMapper.toEntity(dto);
		var managerId = AuthUtil.id(auth);

		if (dto.contacts() != null) {
			contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		}
		if (dto.workingHours() != null) {
			workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);
		}
		var restId = repository.save(restaurant).getId();

		if (AuthUtil.isManager(auth)) {
			managerService.save(restId, managerId);
		}

		return restId;
	}

	@Transactional
	public RestaurantDto update(UUID restId, RestaurantDto dto, Authentication auth) {
		RestaurantEntity restaurant = repository.findById(restId)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", restId));
		managerService.checkAccess(restId, auth);

		restaurantMapper.updateEntity(restaurant, dto);

		for (var c : new ArrayList<>(restaurant.getContacts())) {
			restaurant.removeContact(c);
		}
		for (var wh : new ArrayList<>(restaurant.getWorkingHours())) {
			restaurant.removeWorkingHours(wh);
		}

		repository.flush();

		if (dto.contacts() != null) {
			contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		}
		if (dto.workingHours() != null) {
			workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);
		}
		return restaurantMapper.toDto(restaurant);
	}

	@Transactional(readOnly = true)
	public RestaurantDto findById(UUID id) {
		RestaurantEntity restaurant = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", id));

		return restaurantMapper.toDto(restaurant);
	}

	@Transactional(readOnly = true)
	public List<RestaurantInfoDto> findAll() {
		List<RestaurantEntity> restaurants = repository.findAll();
		return restaurantMapper.toInfoDto(restaurants);
	}

	@Transactional
	public void delete(UUID id, Authentication auth) {
		managerService.checkAccess(id, auth);
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

}
