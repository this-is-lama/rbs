package my.project.restaurantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.NotFoundException;
import my.project.common.security.AuthUtil;
import my.project.restaurantservice.dto.restaurant.RestaurantDto;
import my.project.restaurantservice.dto.restaurant.RestaurantInfoDto;
import my.project.restaurantservice.entity.RestaurantEntity;
import my.project.restaurantservice.mapper.ContactMapper;
import my.project.restaurantservice.mapper.PhotoMapper;
import my.project.restaurantservice.mapper.RestaurantMapper;
import my.project.restaurantservice.mapper.WorkingHoursMapper;
import my.project.restaurantservice.repository.RestaurantRepository;
import my.project.restaurantservice.service.photo.PhotoService;
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
	private final PhotoMapper photoMapper;

	private final RestaurantRepository repository;

	private final ManagerService managerService;
	private final DishService dishService;
	private final TableService tableService;
	private final PhotoService photoService;

	@Transactional
	public UUID save(RestaurantDto dto, Authentication auth) {
		RestaurantEntity restaurant = restaurantMapper.toEntity(dto);
		var managerId = AuthUtil.id(auth);

		contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);

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

		contactMapper.toEntity(dto.contacts()).forEach(restaurant::addContact);
		workingHoursMapper.toEntity(dto.workingHours()).forEach(restaurant::addWorkingHours);

		return restaurantMapper.toDto(restaurant);
	}

	@Transactional
	public void delete(UUID id, Authentication auth) {
		managerService.checkAccess(id, auth);
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public RestaurantDto findById(UUID id, Authentication auth) {
		RestaurantEntity restaurant = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("restaurant.not-found", id));

		if (AuthUtil.isUser(auth)) {
			var dishes = dishService.findAllByRestaurantId(id);
			var tables = tableService.findAllByRestaurantId(id);
			return restaurantMapper.toDto(restaurant, dishes, tables);
		}

		return restaurantMapper.toDto(restaurant);
	}

	@Transactional(readOnly = true)
	public List<RestaurantInfoDto> findAll() {
		var restaurants = repository.findAll();
		var infoRestaurants = restaurantMapper.toInfoDto(restaurants);
		for (var r : infoRestaurants) {
			var bannerPhoto = photoService.findBannerPhoto(r.getId());
			r.setBannerPhoto(photoMapper.toDto(bannerPhoto));
		}
		return infoRestaurants;
	}

	@Transactional(readOnly = true)
	public RestaurantEntity getRef(UUID id) {
		return repository.getReferenceById(id);
	}

}
